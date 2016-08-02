package co.com.alianza.infrastructure.security

import akka.actor._
import akka.pattern.ask
import akka.util.Timeout
import co.com.alianza.commons.enumerations.TiposCliente
import co.com.alianza.exceptions.{ Autorizado, NoAutorizado, Prohibido }
import co.com.alianza.infrastructure.dto.Usuario
import co.com.alianza.infrastructure.dto.security.UsuarioAuth
import co.com.alianza.infrastructure.messages._
import co.com.alianza.infrastructure.security.AuthenticationFailedRejection.{ CredentialsMissing, CredentialsRejected }
import co.com.alianza.util.json.JsonUtil
import co.com.alianza.util.token.{ AesUtil, Token }
import com.typesafe.config.Config
import enumerations.CryptoAesParameters
import oracle.net.aso.r
import portal.transaccional.autenticacion.service.drivers.autorizacion.{ AutorizacionUsuarioEmpresarialRepository, AutorizacionUsuarioRepository }
import spray.http.StatusCodes._
import spray.routing.RequestContext
import spray.routing.authentication.ContextAuthenticator

import scala.concurrent.duration._
import scala.concurrent.{ Future, promise }

trait ServiceAuthorization {
  self: ActorLogging =>

  implicit val system: ActorSystem
  import system.dispatcher
  implicit val conf: Config = system.settings.config

  val autorizacionUsuarioRepo: AutorizacionUsuarioRepository
  val autorizacionAgenteRepo: AutorizacionUsuarioEmpresarialRepository

  implicit val timeout: Timeout = Timeout(10.seconds)

  def authenticateUser: ContextAuthenticator[UsuarioAuth] = {
    ctx =>
      val token = ctx.request.headers.find(header => header.name equals "token")
      log.info(token.toString)
      if (token.isEmpty) {
        Future(Left(AuthenticationFailedRejection(CredentialsMissing, List())))
      } else {
        val encriptedToken: String = token.get.value
        val util = new AesUtil(CryptoAesParameters.KEY_SIZE, CryptoAesParameters.ITERATION_COUNT)
        val decryptedToken = util.decrypt(CryptoAesParameters.SALT, CryptoAesParameters.IV, CryptoAesParameters.PASSPHRASE, encriptedToken)

        val tipoCliente = Token.getToken(decryptedToken).getJWTClaimsSet.getCustomClaim("tipoCliente").toString

        val futuro =
          if (tipoCliente == TiposCliente.agenteEmpresarial.toString) {
            autorizacionAgenteRepo.autorizar(decryptedToken, "", obtenerIp(ctx).get.value)
            //TODO: poner para agente empresarial
            //autorizacionActorSupervisor ? AutorizarUsuarioEmpresarialMessage(token.get.value, None, obtenerIp(ctx).get.value)
          } else if (tipoCliente == TiposCliente.clienteAdministrador.toString) {
            autorizacionUsuarioRepo.autorizarUrl(encriptedToken, "")
            //TODO: poner para empresarial
            //autorizacionActorSupervisor ? AutorizarUsuarioEmpresarialAdminMessage(token.get.value, None)
          } else {
            autorizacionUsuarioRepo.autorizarUrl(decryptedToken, "") // ? AutorizarUrl(token.get.value, "")
          }

        futuro.map {
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
