package co.com.alianza.infrastructure.security

import akka.actor._
import akka.pattern.ask
import akka.event.Logging
import akka.util.Timeout

import co.com.alianza.app.MainActors
import co.com.alianza.commons.enumerations.TiposCliente
import co.com.alianza.infrastructure.dto.{Usuario, UsuarioEmpresarial}
import co.com.alianza.infrastructure.dto.security.UsuarioAuth

import co.com.alianza.infrastructure.messages._
import co.com.alianza.util.json.JsonUtil
import co.com.alianza.util.token.Token

import com.typesafe.config.Config

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future, promise}
import scala.util.{Success, Failure}

import spray.http.StatusCodes._
import spray.http.RemoteAddress
import spray.routing.RequestContext
import spray.routing.authentication.ContextAuthenticator
import spray.routing.AuthenticationFailedRejection
import spray.routing.AuthenticationFailedRejection.{CredentialsRejected, CredentialsMissing}

trait ServiceAuthorization {
  self : ActorLogging =>

  implicit val contextAuthorization: ExecutionContext
  implicit val conf: Config
  implicit val system: ActorSystem
  implicit val timeout: Timeout = Timeout(10 seconds)
//  private[this] val log = Logging(MainActors.system, classOf[ServiceAuthorization])

  def authenticateUser : ContextAuthenticator[UsuarioAuth] = {
    ctx =>
      val token = ctx.request.headers.find(header => header.name equals "token")
      log info(token toString)
      if (token.isEmpty) {
        Future(Left(AuthenticationFailedRejection(CredentialsMissing, List())))
      } else {
        val tipoCliente = Token.getToken(token.get.value).getJWTClaimsSet.getCustomClaim("tipoCliente").toString
        val p = promise[Any]
        var futuro: Future[Any] = null
        if (tipoCliente == TiposCliente.agenteEmpresarial.toString)
          futuro = MainActors.autorizacionActorSupervisor ? AutorizarUsuarioEmpresarialMessage(token.get.value, None, obtenerIp(ctx).get.value)
        else if (tipoCliente == TiposCliente.clienteAdministrador.toString)
          futuro = MainActors.autorizacionActorSupervisor ? AutorizarUsuarioEmpresarialAdminMessage(token.get.value, None)
        else
          futuro = MainActors.autorizacionActorSupervisor ? AutorizarUrl(token.get.value, "")
        futuro map {
          case r: ResponseMessage =>
            r.statusCode match {
              case Unauthorized => Left(AuthenticationFailedRejection(CredentialsRejected, List()))
              case OK =>
                val user = JsonUtil.fromJson[Usuario](r.responseBody)
                Right(UsuarioAuth(user.id.get, user.tipoCliente))
              case Forbidden =>
                val user = JsonUtil.fromJson[UsuarioForbidden](r.responseBody)
                Right(UsuarioAuth(user.usuario.id.get, user.usuario.tipoCliente))

            }
          case _ =>
            Left(AuthenticationFailedRejection(CredentialsRejected, List()))
        }
      }
  }

  private def obtenerIp(ctx: RequestContext) = ctx.request.headers.find {
    header =>
      header.name.equals("Remote-Address") || header.name.equals("X-Forwarded-For") || header.name.equals("X-Real-IP")//TODO: Mejorar este método
  }

}

case class UsuarioForbidden(usuario: Usuario, filtro: String, code: String)
