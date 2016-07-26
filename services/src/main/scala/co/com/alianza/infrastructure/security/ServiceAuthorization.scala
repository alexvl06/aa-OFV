package co.com.alianza.infrastructure.security

import akka.actor._
import akka.pattern.ask
import akka.util.Timeout

import co.com.alianza.commons.enumerations.TiposCliente
import co.com.alianza.infrastructure.dto.Usuario
import co.com.alianza.infrastructure.dto.security.UsuarioAuth
import co.com.alianza.infrastructure.messages._
import co.com.alianza.infrastructure.security.AuthenticationFailedRejection.{ CredentialsMissing, CredentialsRejected }
import co.com.alianza.util.json.JsonUtil
import co.com.alianza.util.token.{ AesUtil, Token }
import com.typesafe.config.Config
import enumerations.CryptoAesParameters
import spray.http.StatusCodes._
import spray.routing.RequestContext
import spray.routing.authentication.ContextAuthenticator

import scala.concurrent.duration._
import scala.concurrent.{ Future, promise }

trait ServiceAuthorization {
  self: ActorLogging =>

  implicit val system: ActorSystem
  import system.dispatcher

  implicit val autorizacionActorSupervisor: ActorRef
  implicit val conf: Config = system.settings.config
  implicit val timeout: Timeout = Timeout(10.seconds)

  def authenticateUser: ContextAuthenticator[UsuarioAuth] = {
    ctx =>
      val token = ctx.request.headers.find(header => header.name equals "token")
      log info (token.toString)
      if (token.isEmpty) {
        Future(Left(AuthenticationFailedRejection(CredentialsMissing, List())))
      } else {
        var util = new AesUtil(CryptoAesParameters.KEY_SIZE, CryptoAesParameters.ITERATION_COUNT)
        var decryptedToken = util.decrypt(CryptoAesParameters.SALT, CryptoAesParameters.IV, CryptoAesParameters.PASSPHRASE, token.get.value)
        val tipoCliente = Token.getToken(decryptedToken).getJWTClaimsSet.getCustomClaim("tipoCliente").toString
        val p = promise[Any]
        var futuro: Future[Any] = null
        if (tipoCliente == TiposCliente.agenteEmpresarial.toString)
          futuro = autorizacionActorSupervisor ? AutorizarUsuarioEmpresarialMessage(token.get.value, None, obtenerIp(ctx).get.value)
        else if (tipoCliente == TiposCliente.clienteAdministrador.toString)
          futuro = autorizacionActorSupervisor ? AutorizarUsuarioEmpresarialAdminMessage(token.get.value, None)
        else
          futuro = autorizacionActorSupervisor ? AutorizarUrl(token.get.value, "")
        futuro map {
          case r: ResponseMessage =>
            r.statusCode match {
              case Unauthorized =>
                Left(AuthenticationFailedRejection(CredentialsRejected, List(), Some(Unauthorized.intValue), Some(r.responseBody)))
              case OK =>
                val user = JsonUtil.fromJson[Usuario](r.responseBody)
                Right(UsuarioAuth(user.id.get, user.tipoCliente, user.identificacion, user.tipoIdentificacion))
              case Forbidden =>
                val user = JsonUtil.fromJson[UsuarioForbidden](r.responseBody)
                Right(UsuarioAuth(user.usuario.id.get, user.usuario.tipoCliente, user.usuario.identificacion, user.usuario.tipoIdentificacion))

            }
          case _ =>
            Left(AuthenticationFailedRejection(CredentialsRejected, List()))
        }
      }
  }

  private def obtenerIp(ctx: RequestContext) = ctx.request.headers.find {
    header =>
      header.name.equals("X-Forwarded-For") || header.name.equals("X-Real-IP") || header.name.equals("Remote-Address")
  }

}

case class UsuarioForbidden(usuario: Usuario, filtro: String, code: String)
