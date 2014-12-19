package co.com.alianza.infrastructure.security

import akka.actor._
import akka.pattern.ask
import akka.event.Logging
import akka.util.Timeout

import co.com.alianza.app.MainActors
import co.com.alianza.infrastructure.dto.{Usuario, UsuarioEmpresarial}
import co.com.alianza.infrastructure.dto.security.UsuarioAuth

import co.com.alianza.infrastructure.messages._
import co.com.alianza.util.json.JsonUtil

import com.typesafe.config.Config

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future, promise}
import scala.util.{Success, Failure}

import spray.http.StatusCodes._
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

  def authenticateUser: ContextAuthenticator[UsuarioAuth] = {
    ctx =>

      val token = ctx.request.headers.find(header => header.name equals "token")
      if (token.isEmpty) {
        Future(Left(AuthenticationFailedRejection(CredentialsMissing, List())))
      } else {
        val p = promise[Any]
        MainActors.usuariosActorSupervisor ? ConsultaUsuarioMessage(token = Some(token.get.value)) onComplete {
          case Success(Some(usuario)) =>
            val uf = MainActors.autorizacionActorSupervisor ? AutorizarUrl(token.get.value, "")
            log info ("Individual: "+usuario.toString)
            uf onSuccess { case r => p.trySuccess(r) }
            uf onFailure { case t => p.tryFailure(t) }
          case Success(None) =>
            MainActors.usuariosActorSupervisor ? ConsultaUsuarioEmpresarialMessage(token = Some(token.get.value)) onComplete {
              case Success(Some(usuario)) =>
                val uf = MainActors.autorizacionActorSupervisor ? AutorizarUsuarioEmpresarialMessage(token.get.value)
                uf onSuccess { case r => p.trySuccess(r) }
                uf onFailure { case t => p.tryFailure(t) }
              case Success(None) =>
                MainActors.usuariosActorSupervisor ? ConsultaUsuarioEmpresarialAdminMessage(token = Some(token.get.value)) onComplete {
                  case Success(Some(usuario)) =>
                    val uf = MainActors.autorizacionActorSupervisor ? AutorizarUsuarioEmpresarialAdminMessage(token.get.value)
                    uf onSuccess { case r =>  p.trySuccess(r) }
                    uf onFailure { case _ => p.tryFailure(_) }
                  case Success(None) =>
                    p.trySuccess(None)
                  case Failure(t) =>
                    p.tryFailure(t)
                }
              case Failure(t) =>
                p.tryFailure(t)
            }
          case Failure(t) =>
            p.tryFailure(t)
        }
        p.future map {
          case r: ResponseMessage =>
            r.statusCode match {
              case Unauthorized => Left(AuthenticationFailedRejection(CredentialsRejected, List()))
              case OK =>
                val user = JsonUtil.fromJson[Usuario](r.responseBody)
                Right(UsuarioAuth(user.id.get))
              case Forbidden =>
                val id = r.responseBody.substring(17, 20)
                Right(UsuarioAuth(id.toInt))

            }
          case _ =>
            Left(AuthenticationFailedRejection(CredentialsRejected, List()))
        }

      }
  }

}