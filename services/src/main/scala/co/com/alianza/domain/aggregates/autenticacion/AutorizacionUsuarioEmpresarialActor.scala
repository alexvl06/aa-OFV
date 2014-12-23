package co.com.alianza.domain.aggregates.autenticacion

import akka.pattern.ask

import co.com.alianza.util.token.Token
import co.com.alianza.util.transformers.ValidationT
import co.com.alianza.infrastructure.messages._
import co.com.alianza.util.json.JsonUtil
import co.com.alianza.infrastructure.dto.{UsuarioEmpresarial, UsuarioEmpresarialAdmin}
import co.com.alianza.infrastructure.anticorruption.usuarios.{DataAccessAdapter => usDataAdapter}
import co.com.alianza.exceptions.PersistenceException
import co.com.alianza.app.MainActors

import scala.concurrent.Future
import scalaz.std.AllInstances._
import scalaz.Validation

import spray.http.StatusCodes._

/**
 * Created by manuel on 16/12/14.
 */
class AutorizacionUsuarioEmpresarialActor extends AutorizacionActor {

  override def receive = {

    case message: AutorizarUsuarioEmpresarialMessage =>
      val currentSender = sender()
      val future = (for {
        usuarioOption <- ValidationT(validarToken(message.token))
        result <- ValidationT(validarUsuario(usuarioOption))
      } yield {
        result
      }).run
      resolveFutureValidation(future, (x: ResponseMessage) => x, currentSender)

    case message: AutorizarUsuarioEmpresarialAdminMessage =>
      val currentSender = sender()
      val future = (for {
        usuarioOption <- ValidationT(validarTokenAdmin(message.token))
        result <- ValidationT(validarUsuario(usuarioOption))
      } yield {
        result
      }).run
      resolveFutureValidation(future, (x: ResponseMessage) => x, currentSender)

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
   * Realiza la validación del Token, llamando a [[Token.autorizarToken]]
   * Retorna un futuro con un Validationm, donde el caso de contiene el Option[Usuario]
   *
   *
   * @param token El token para realizar validación
   */
  private def validarTokenAdmin(token: String): Future[Validation[PersistenceException, Option[UsuarioEmpresarialAdmin]]] = {
    Token.autorizarToken(token) match {
      case true =>
        usDataAdapter.obtenerUsuarioEmpresarialAdminToken(token).flatMap { x =>
          val y: Validation[PersistenceException, Future[Option[UsuarioEmpresarialAdmin]]] = x.map { userOpt =>
            guardaTokenAdminCache(userOpt, token)
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
      case true => usuarioOption.map(usuario => usuario.copy(contrasena = None))
      case false => None
    }
  }

  /**
   *
   * Si usuarioOption tiene un valor se guarda en cache y retorna el usuario sin el campo contraseña
   * @param usuarioOption Option con el usuario
   * @param token El token
   * @return
   */
  private def guardaTokenAdminCache(usuarioOption: Option[UsuarioEmpresarialAdmin], token: String): Future[Option[UsuarioEmpresarialAdmin]] = {

    val validacionSesion: Future[Boolean] = ask(MainActors.sesionActorSupervisor, ValidarSesion(token)).mapTo[Boolean]
    validacionSesion.map {
      case true => usuarioOption.map(usuario => usuario.copy(contrasena = None))
      case false => None
    }
  }

  /**
   * Valida si el usuario existe y responde
   * @param usuarioOp Option
   * @return ResponseMessage con el statusCode correspondiente
   */
  private def validarUsuario(usuarioOp: Option[Any]) = {
    usuarioOp match {
      case None => Future.successful(Validation.success(ResponseMessage(Unauthorized, "Error Validando Token")))
      case Some(us) => Future.successful(Validation.success(ResponseMessage(OK, JsonUtil.toJson(us))))
    }
  }

}