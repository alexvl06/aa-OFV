package co.com.alianza.domain.aggregates.autenticacion

import co.com.alianza.util.token.Token
import co.com.alianza.util.transformers.ValidationT
import co.com.alianza.infrastructure.messages._
import spray.http.StatusCodes._

import co.com.alianza.infrastructure.dto.UsuarioEmpresarial
import co.com.alianza.infrastructure.anticorruption.usuarios.{DataAccessAdapter => usDataAdapter}
import co.com.alianza.infrastructure.anticorruption.recursos.{DataAccessAdapter => rDataAccessAdapter}
import scala.concurrent.duration._
import scala.concurrent.Future
import scalaz.std.AllInstances._
import scala.util.{Success, Failure}
import scalaz.Validation
import co.com.alianza.exceptions.PersistenceException
import co.com.alianza.app.MainActors
import akka.pattern.ask

/**
 * Created by manuel on 16/12/14.
 */
class AutorizacionUsuarioEmpresarialActor extends AutorizacionActor {

  override def receive = {
    case message: AutorizarUsuarioEmpresarialUrl =>
      val currentSender = sender()

      val future = (for {
        usuarioOption <- ValidationT(validarToken(message.token))
//        resultAutorizar <- ValidationT(validarRecurso(usuarioOption, message.url))
      } yield {
        usuarioOption
      }).run

      resolveFutureValidation(future, (x: Option[UsuarioEmpresarial]) => x, currentSender)


//    case message: InvalidarToken =>
//
//      val currentSender = sender()
//      val futureInvalidarToken = usDataAdapter.invalidarTokenUsuario(message.token)
//
//      futureInvalidarToken onComplete {
//        case Failure(failure) => currentSender ! failure
//        case Success(value) =>
//          MainActors.sesionActorSupervisor ! InvalidarSesion(message.token)
//          currentSender ! ResponseMessage(OK, "El token ha sido removido")
//      }
  }

  /**
   * Realiza la validación del Token, llamando a [[Token.autorizarToken]]
   * Retorna un futuro con un Validationm, donde el caso de contiene el Option[Usuario]
   *
   *
   * @param token El token para realizar validación
   */
  private def validarToken(token: String): Future[Validation[PersistenceException, Option[UsuarioEmpresarial]]] = {
    Token.autorizarToken(token) match {
      case true =>
        usDataAdapter.obtenerUsuarioEmpresarialToken(token).flatMap { x =>
          val y: Validation[PersistenceException, Future[Option[UsuarioEmpresarial]]] = x.map { userOpt =>
            guardaTokenCache(userOpt, token)
          }
          co.com.alianza.util.transformers.Validation.sequence(y)
        }
      case false =>
        Future.successful(Validation.success(None))
    }
  }

  /**
   *
   * Si usuarioOption tiene un valor se guarda en cache y retorna el usuario sin el campo contraseña
   * @param usuarioOption Option con el usuario
   * @param token El token
   * @return
   */
  private def guardaTokenCache(usuarioOption: Option[UsuarioEmpresarial], token: String): Future[Option[UsuarioEmpresarial]] = {

    val validacionSesion: Future[Boolean] = ask(MainActors.sesionActorSupervisor, ValidarSesion(token)).mapTo[Boolean]
    validacionSesion.map {
      case true => usuarioOption.map ( usuario =>usuario.copy(contrasena = None))
      case false => None
    }
  }

}
