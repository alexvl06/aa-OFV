package co.com.alianza.infrastructure.security

import scala.concurrent.{ExecutionContext, Future}
import spray.routing.{Rejection, AuthenticationFailedRejection}
import co.com.alianza.infrastructure.dto.security.UsuarioAuth
import spray.routing.authentication.ContextAuthenticator
import com.typesafe.config.Config
import akka.actor.ActorSystem
import co.com.alianza.util.token.Token
import spray.routing.AuthenticationFailedRejection.{CredentialsRejected, CredentialsMissing}
import co.com.alianza.infrastructure.anticorruption.usuarios.DataAccessAdapter
import co.com.alianza.exceptions.PersistenceException
import co.com.alianza.infrastructure.dto.{Usuario}
import scalaz.{Failure => zFailure, Success => zSuccess, Validation}

trait ServiceAuthorization {

  implicit val contextAuthorization: ExecutionContext
  implicit val conf: Config
  implicit val system: ActorSystem

  def authenticateUser: ContextAuthenticator[UsuarioAuth] = { ctx =>
  {
    val token = ctx.request.headers.find(header => header.name equals "token")

    if (token.isEmpty) {
      Future(Left(AuthenticationFailedRejection(CredentialsMissing, List())))
    } else {

      Token.autorizarToken( token.get.value ) match {
        case true =>
          val usuario: Future[Validation[PersistenceException, Option[Usuario]]] = DataAccessAdapter.obtenerUsuarioToken(token.get.value)
          val future: Future[Either[Rejection, UsuarioAuth]] = usuario.map(x =>resolveObtenerToken(x))
          future
        case false =>
          Future(Left(AuthenticationFailedRejection(CredentialsRejected, List())))

      }
    }
  }
  }

  private def resolveObtenerToken(usuario: Validation[PersistenceException, Option[Usuario]]):Either[Rejection, UsuarioAuth] = {
    usuario match{
      case zFailure(error) =>
        Left(AuthenticationFailedRejection(CredentialsRejected,List()))
      case zSuccess(value) =>
        value match {
          case Some(usu) => Right(UsuarioAuth(usu.id.get))
          case _ =>  Left(AuthenticationFailedRejection(CredentialsRejected,List()))
        }
    }
  }

}



