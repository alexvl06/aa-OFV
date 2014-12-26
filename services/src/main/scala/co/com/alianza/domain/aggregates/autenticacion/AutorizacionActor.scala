package co.com.alianza.domain.aggregates.autenticacion

import java.util.Date

import akka.actor.{ActorLogging, Actor}
import akka.actor.Props
import akka.pattern.ask
import akka.routing.RoundRobinPool
import akka.util.Timeout

import co.com.alianza.app.MainActors
import co.com.alianza.constants.TiposConfiguracion
import co.com.alianza.exceptions.PersistenceException
import co.com.alianza.infrastructure.anticorruption.configuraciones.{DataAccessAdapter => confDataAdapter}
import co.com.alianza.infrastructure.anticorruption.usuarios.{DataAccessAdapter => usDataAdapter}
import co.com.alianza.infrastructure.anticorruption.recursos.{DataAccessAdapter => rDataAccessAdapter}
import co.com.alianza.infrastructure.dto.{Configuracion, RecursoUsuario, Usuario}
import co.com.alianza.infrastructure.messages._
import co.com.alianza.util.FutureResponse
import co.com.alianza.util.json.JsonUtil
import co.com.alianza.util.token.Token
import co.com.alianza.util.transformers.ValidationT

import spray.http.StatusCodes._

import scala.concurrent.duration._
import scala.concurrent.Future
import scalaz.std.AllInstances._
import scala.util.{Success, Failure}
import scalaz.Validation
import scalaz.{Failure => zFailure, Success => zSuccess, Validation}

class AutorizacionActorSupervisor extends Actor with ActorLogging {

  import akka.actor.SupervisorStrategy._
  import akka.actor.OneForOneStrategy

  val autorizacionActor = context.actorOf(Props[AutorizacionActor].withRouter(RoundRobinPool(nrOfInstances = 1)), "autorizacionActor")
  val autorizacionUsuarioEmpresarialActor = context.actorOf(Props[AutorizacionUsuarioEmpresarialActor].withRouter(RoundRobinPool(nrOfInstances = 1)), "autorizacionUsuarioEmpresarialActor")

  def receive = {

    case m: AutorizarUsuarioEmpresarialMessage =>
      autorizacionUsuarioEmpresarialActor forward m
      log info (m toString)

    case m: AutorizarUsuarioEmpresarialAdminMessage =>
      autorizacionUsuarioEmpresarialActor forward m
      log info (m toString)

    case message: Any =>
      autorizacionActor forward message; log info (message toString)

  }

  override val supervisorStrategy = OneForOneStrategy() {
    case exception: Exception =>
      exception.printStackTrace()
      log.error(exception, exception.getMessage)
      Restart
  }

}

/**
 * Realiza la validación de un token y si se está autorizado para acceder a la url
 * @author smontanez
 */
class AutorizacionActor extends Actor with ActorLogging with FutureResponse {

  import scala.concurrent.ExecutionContext

  implicit val _: ExecutionContext = context.dispatcher
  implicit val timeout: Timeout = 10.seconds

  def receive = {
    case message: AutorizarUrl =>
      val currentSender = sender()

      val future = (for {
        usuarioOption <- ValidationT(validarToken(message.token))
        resultAutorizar <- ValidationT(validarRecurso(usuarioOption, message.url))
      } yield {
        resultAutorizar
      }).run
      resolveFutureValidation(future, (x: ResponseMessage) => x, currentSender)
    case message: InvalidarToken =>

      val currentSender = sender()
      val futureInvalidarToken = usDataAdapter.invalidarTokenUsuario(message.token)

      futureInvalidarToken onComplete {
        case Failure(failure) => currentSender ! failure
        case Success(value) =>
          MainActors.sesionActorSupervisor ! InvalidarSesion(message.token)
          currentSender ! ResponseMessage(OK, "El token ha sido removido")
      }
  }

  /**
   * Realiza la validación del Token, llamando a [[Token.autorizarToken]]
   * Retorna un futuro con un Validationm, donde el caso de contiene el Option[Usuario]
   *
   *
   * @param token El token para realizar validación
   */
  private def validarToken(token: String): Future[Validation[PersistenceException, Option[Usuario]]] = {
    Token.autorizarToken(token) match {
      case true =>
        usDataAdapter.obtenerUsuarioToken(token).flatMap { x =>
          val y: Validation[PersistenceException, Future[Option[Usuario]]] = x.map { userOpt =>
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
  private def guardaTokenCache(usuarioOption: Option[Usuario], token: String): Future[Option[Usuario]] = {

    val validacionSesion: Future[Boolean] = ask(MainActors.sesionActorSupervisor, ValidarSesion(token)).mapTo[Boolean]
    validacionSesion.map {
      case true => usuarioOption.map ( usuario =>usuario.copy(contrasena = None))
      case false => None
    }
  }

  /**
   *
   * Se valida si el recurso solicitado esta asociado al usuario
   *
   * @return
   */
  private def validarRecurso(usuarioOpt: Option[Usuario], url: String) = {

    usuarioOpt match {
      case Some(usuario) =>
        val recursosFuturo = rDataAccessAdapter.obtenerRecursos(usuario.id.get)
        recursosFuturo.map(_.map(x => resolveMessageRecursos(usuario, x.filter(filtrarRecursos(_, url)))))
      case _ =>
        Future.successful(Validation.success(ResponseMessage(Unauthorized, "Error Validando Token")))
    }

  }

  /**
   * De acuerdo si la lista tiene contenido retorna un ResponseMessage
   *
   * @param recursos Listado de recursos
   * @return
   */
  private def resolveMessageRecursos(usuario: Usuario, recursos: List[RecursoUsuario]) = {
    recursos.isEmpty match {
      case true => ResponseMessage(Forbidden, JsonUtil.toJson(ForbiddenMessage(usuario, None, "403.1")))
      case false =>
        val recurso = recursos.head

        recurso.filtro match {
          case Some(filtro) => ResponseMessage(Forbidden, JsonUtil.toJson(ForbiddenMessage(usuario, recurso.filtro, "403.2")))
          case None => ResponseMessage(OK, JsonUtil.toJson(usuario))

        }

    }
  }

  /**
   * Filtra el listado de recursos que concuerden con la url
   *
   * @param recurso recursos asociados al usuario
   * @param url la url a validar
   * @return
   */
  private def filtrarRecursos(recurso: RecursoUsuario, url: String): Boolean = {
    if (recurso.urlRecurso.equals(url))
      recurso.acceso
    else if (recurso.urlRecurso.endsWith("/*")) {
      val urlC = recurso.urlRecurso.substring(0, recurso.urlRecurso.lastIndexOf("*"))
      if (urlC.equals(url + "/")) recurso.acceso
      else {
        if (url.length >= urlC.length) {
          val ends = if (url.endsWith("/")) "" else ""
          val urlSuffix = url.substring(0, urlC.length) + ends
          if (urlSuffix.equals(urlC)) recurso.acceso
          else false
        } else false
      }

    } else false
  }

}

case class ForbiddenMessage(usuario: Usuario, filtro: Option[String], code: String)