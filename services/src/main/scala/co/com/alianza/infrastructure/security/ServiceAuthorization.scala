package co.com.alianza.infrastructure.security

import akka.actor._
import akka.util.Timeout
import co.com.alianza.commons.enumerations.TiposCliente
import co.com.alianza.exceptions.{ ValidacionException, Autorizado, NoAutorizado, Prohibido }
import co.com.alianza.infrastructure.dto.Usuario
import co.com.alianza.infrastructure.dto.security.UsuarioAuth
import co.com.alianza.infrastructure.security.AuthenticationFailedRejection.{ CredentialsMissing, CredentialsRejected }
import co.com.alianza.util.json.JsonUtil
import co.com.alianza.util.token.{ AesUtil, Token }
import com.typesafe.config.Config
import portal.transaccional.autenticacion.service.drivers.autorizacion.{ AutorizacionUsuarioEmpresarialAdminRepository, AutorizacionUsuarioEmpresarialRepository, AutorizacionUsuarioRepository }
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

  implicit val timeout: Timeout = Timeout(10.seconds)

  def authenticateUser: ContextAuthenticator[UsuarioAuth] = {
    ctx =>
      val token = ctx.request.headers.find(header => header.name equals "token")

      log.info(token.toString)
      if (token.isEmpty) {
        Future(Left(AuthenticationFailedRejection(CredentialsMissing, List())))
      } else {
        val encriptedToken: String = token.get.value
        val decryptedToken = AesUtil.desencriptarToken(encriptedToken, "ServiceAuthorization.authenticateUser")

        val tipoCliente: String = Token.getToken(decryptedToken).getJWTClaimsSet.getCustomClaim("tipoCliente").toString

        val futuro =
          if (tipoCliente == TiposCliente.agenteEmpresarial.toString) {
            autorizacionAgenteRepo.autorizar(decryptedToken, "", obtenerIp(ctx).get.value)
          } else if (tipoCliente == TiposCliente.clienteAdministrador.toString) {
            autorizacionAdminRepo.autorizar(decryptedToken, "", obtenerIp(ctx).get.value)
          } else {
            autorizacionUsuarioRepo.autorizarUrl(decryptedToken, "")
          }

        futuro.map {
          case validacion: ValidacionException =>
            Left(AuthenticationFailedRejection(CredentialsRejected, List(), Some(Unauthorized.intValue), Option(validacion.code)))
          case validacion: NoAutorizado =>
            Left(AuthenticationFailedRejection(CredentialsRejected, List(), Some(Unauthorized.intValue), None))
          case validacion: Autorizado =>
            val user = JsonUtil.fromJson[Usuario](validacion.usuario)
            Right(UsuarioAuth(user.id.get, user.tipoCliente, user.identificacion, user.tipoIdentificacion))
          case validacion: Prohibido =>
            val user = JsonUtil.fromJson[UsuarioForbidden](validacion.usuario)
            Right(UsuarioAuth(user.usuario.id.get, user.usuario.tipoCliente, user.usuario.identificacion, user.usuario.tipoIdentificacion))
          case ex: Any =>
            Left(AuthenticationFailedRejection(CredentialsRejected, List()))
        }
      }
  }

  private def obtenerIp(ctx: RequestContext) = ctx.request.headers.find {
    header =>
      header.name.equals("X-Forwarded-For") || header.name.equals("X-Real-IP") || header.name.equals("Remote-Address")
  }

}

case class UsuarioForbidden(usuario: Usuario, filtro: String)
