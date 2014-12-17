package co.com.alianza.infrastructure.security

import akka.actor._
import akka.pattern.ask
import akka.util.Timeout

import co.com.alianza.app.MainActors
import co.com.alianza.infrastructure.dto.{Usuario, UsuarioEmpresarial}
import co.com.alianza.infrastructure.dto.security.UsuarioAuth

import co.com.alianza.infrastructure.messages.{AutorizarUrl, ResponseMessage, AutorizarUsuarioEmpresarialUrl}
import co.com.alianza.util.json.JsonUtil

import com.typesafe.config.Config

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Success, Failure}

import spray.http.StatusCodes._
import spray.routing.authentication.ContextAuthenticator
import spray.routing.AuthenticationFailedRejection
import spray.routing.AuthenticationFailedRejection.{CredentialsRejected, CredentialsMissing}

trait ServiceAuthorization {

  implicit val contextAuthorization: ExecutionContext
  implicit val conf: Config
  implicit val system: ActorSystem
  implicit val timeout: Timeout = Timeout(10 seconds)

  def authenticateUser: ContextAuthenticator[UsuarioAuth] = {
    ctx =>

      val token = ctx.request.headers.find(header => header.name equals "token")
      if (token.isEmpty) {
        Future(Left(AuthenticationFailedRejection(CredentialsMissing, List())))
      } else {
        val x: Future[Any] = MainActors.autorizacionActorSupervisor ? AutorizarUrl(token.get.value, "")
        x map {
          case r: ResponseMessage =>
            println("*****"+r.toString)
            r.statusCode match {
              case Unauthorized => Left(AuthenticationFailedRejection(CredentialsRejected, List()))
              case OK =>
                val user = JsonUtil.fromJson[Usuario](r.responseBody)
                Right(UsuarioAuth(user.id.get))
              case Forbidden =>
                val user = JsonUtil.fromJson[UsuarioForbidden](r.responseBody)
                Right(UsuarioAuth(user.usuario.id.get))

            }
          case _ =>
            Left(AuthenticationFailedRejection(CredentialsRejected, List()))
        }
      }
  }

}

case class UsuarioForbidden(usuario: Usuario, filtro: String, code: String)
