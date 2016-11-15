package co.com.alianza.infrastructure.security

import akka.actor._
import akka.util.Timeout
import co.com.alianza.commons.enumerations.TiposCliente
import co.com.alianza.exceptions._
import co.com.alianza.infrastructure.dto.Usuario
import co.com.alianza.infrastructure.dto.security.UsuarioAuth
import co.com.alianza.infrastructure.security.AuthenticationFailedRejection.{ CredentialsMissing, CredentialsRejected }
import co.com.alianza.persistence.entities.{ UsuarioComercial, UsuarioComercialAdmin }
import co.com.alianza.util.json.JsonUtil
import co.com.alianza.util.token.{ AesUtil, Token }
import com.typesafe.config.Config
import portal.transaccional.autenticacion.service.drivers.autorizacion._
import spray.http.StatusCodes._
import spray.routing.RequestContext
import spray.routing.authentication.ContextAuthenticator

import scala.concurrent.Future
import scala.concurrent.duration._

trait ServiceAuthorization {
  self: ActorLogging =>

  implicit val system: ActorSystem
  import system.dispatcher
  implicit val conf: Config = system.settings.config

  val autorizacionUsuarioRepo: AutorizacionUsuarioRepository
  val autorizacionAgenteRepo: AutorizacionUsuarioEmpresarialRepository
  val autorizacionAdminRepo: AutorizacionUsuarioEmpresarialAdminRepository
  val autorizacionComercialRepo: AutorizacionUsuarioComercialRepository
  val autorizacionComercialAdminRepo: AutorizacionUsuarioComercialAdminRepository

  implicit val timeout: Timeout = Timeout(10.seconds)

  def authenticateUser: ContextAuthenticator[UsuarioAuth] = {
    ctx =>
      val tokenRequest = ctx.request.headers.find(header => header.name equals "token")

      log.info(tokenRequest.toString)
      if (tokenRequest.isEmpty) {
        Future(Left(AuthenticationFailedRejection(CredentialsMissing, List())))
      } else {
        val encriptedToken: String = tokenRequest.get.value
        val token = AesUtil.desencriptarToken(encriptedToken)
        val tipoCliente: String = Token.getToken(token).getJWTClaimsSet.getCustomClaim("tipoCliente").toString
        val futuro: Future[ValidacionAutorizacion] = {
          if (tipoCliente == TiposCliente.agenteEmpresarial.toString) {
            autorizacionAgenteRepo.autorizar(token, encriptedToken, "", obtenerIp(ctx).get.value)
          } else if (tipoCliente == TiposCliente.clienteAdministrador.toString) {
            autorizacionAdminRepo.autorizar(token, encriptedToken, "", obtenerIp(ctx).get.value)
          } else if (tipoCliente == TiposCliente.clienteIndividual.toString) {
            autorizacionUsuarioRepo.autorizar(token, encriptedToken, "")
          } else if (tipoCliente == TiposCliente.comercialFiduciaria.toString) {
            autorizacionComercialRepo.autorizarFiduciaria(token, encriptedToken, "")
          } else if (tipoCliente == TiposCliente.comercialValores.toString) {
            autorizacionComercialRepo.autorizarValores(token, encriptedToken, "")
          } else if (tipoCliente == TiposCliente.comercialSAC.toString) {
            autorizacionComercialRepo.autorizarSAC(token, encriptedToken, "")
          } else if (tipoCliente == TiposCliente.comercialAdmin.toString) {
            autorizacionComercialAdminRepo.autorizar(token, encriptedToken, "")
          } else {
            Future.failed(NoAutorizado("tipo de usuario no autorizado"))
          }
        }
        //mapear las respuestas y los errores
        futuro.map {
          x => resolverValidacion(x, tipoCliente)
        } recover {
          case error => resolverValidacion(error, tipoCliente)
        }
      }
  }

  private def resolverValidacion(respuesta: Any, tipoCliente: String): Either[AuthenticationFailedRejection, UsuarioAuth] with Product with Serializable = {

    respuesta match {

      case validacion: Autorizado =>
        val usuarioAuth: UsuarioAuth = JsonUtil.fromJson[UsuarioAuth](validacion.usuario)
        Right(usuarioAuth)

      case validacion: AutorizadoComercial =>
        val tipo = TiposCliente.getTipoCliente(tipoCliente)
        val user = JsonUtil.fromJson[UsuarioComercial](validacion.usuario)
        Right(UsuarioAuth(user.id, tipo, "", 0))

      case validacion: AutorizadoComercialAdmin =>
        val tipo = TiposCliente.getTipoCliente(tipoCliente)
        val user = JsonUtil.fromJson[UsuarioComercialAdmin](validacion.usuario)
        Right(UsuarioAuth(user.id, tipo, "", 0, Option(user.usuario)))

      case validacion: ValidacionException =>
        validacion.printStackTrace()
        Left(AuthenticationFailedRejection(CredentialsRejected, List(), Some(Unauthorized.intValue), Option(validacion.code)))

      case validacion: NoAutorizado =>
        validacion.printStackTrace()
        Left(AuthenticationFailedRejection(CredentialsRejected, List(), Some(Unauthorized.intValue), None))

      case validacion: Prohibido =>
        validacion.printStackTrace()
        Left(AuthenticationFailedRejection(CredentialsRejected, List(), Some(Forbidden.intValue), None))

      case _ =>
        Left(AuthenticationFailedRejection(CredentialsRejected, List()))
    }
  }

  private def obtenerIp(ctx: RequestContext) = ctx.request.headers.find {
    header =>
      header.name.equals("X-Forwarded-For") || header.name.equals("X-Real-IP") || header.name.equals("Remote-Address")
  }

}

case class UsuarioForbidden(usuario: Usuario, filtro: String)
