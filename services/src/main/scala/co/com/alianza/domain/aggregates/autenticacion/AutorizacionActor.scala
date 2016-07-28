package co.com.alianza.domain.aggregates.autenticacion

import java.util.Date

import akka.actor.{ Actor, ActorLogging, ActorRef, ActorSystem, Props }
import akka.pattern.ask
import akka.routing.RoundRobinPool
import akka.util.Timeout
import co.com.alianza.constants.TiposConfiguracion
import co.com.alianza.domain.aggregates.autenticacion.errores.TokenInvalido
import co.com.alianza.exceptions.PersistenceException
import co.com.alianza.infrastructure.anticorruption.configuraciones.{ DataAccessAdapter => confDataAdapter }
import co.com.alianza.infrastructure.anticorruption.usuarios.{ DataAccessAdapter => usDataAdapter }
import co.com.alianza.infrastructure.anticorruption.recursos.{ DataAccessAdapter => rDataAccessAdapter }
import co.com.alianza.infrastructure.dto.{ Configuracion, RecursoUsuario, Usuario }
import co.com.alianza.infrastructure.messages._
import co.com.alianza.util.FutureResponse
import co.com.alianza.util.json.JsonUtil
import co.com.alianza.util.token.{ AesUtil, Token }
import co.com.alianza.util.transformers.ValidationT
import enumerations.CryptoAesParameters
import spray.http.StatusCodes._

import scala.concurrent.duration._
import scala.concurrent.Future
import scala.util.{ Failure, Success }
import scalaz.Validation
import scalaz.{ Validation, Failure => zFailure, Success => zSuccess }

object AutorizacionActorSupervisor {

  def props(sesionActorSupervisor: ActorRef) = Props(AutorizacionActorSupervisor(sesionActorSupervisor))
}

case class AutorizacionActorSupervisor(sesionActorSupervisor: ActorRef) extends Actor with ActorLogging {

  import akka.actor.SupervisorStrategy._
  import akka.actor.OneForOneStrategy

  val autorizacionActor = context.actorOf(AutorizacionActor.props(sesionActorSupervisor).withRouter(RoundRobinPool(nrOfInstances = 1)), "autorizacionActor")
  val autorizacionUsuarioEmpresarialActor = context.actorOf(
    Props[AutorizacionUsuarioEmpresarialActor].withRouter(RoundRobinPool(nrOfInstances = 1)),
    "autorizacionUsuarioEmpresarialActor"
  )

  def receive: PartialFunction[Any, Unit] = {

    case m: AutorizarUsuarioEmpresarialMessage =>
      autorizacionUsuarioEmpresarialActor forward m; log.info(m.toString)

    case m: AutorizarUsuarioEmpresarialAdminMessage =>
      autorizacionUsuarioEmpresarialActor forward m; log.info(m.toString)

    case message: Any => println("LO ESTOY MANEJANDO !!"); autorizacionActor forward message; log.info(message.toString)

  }

  override val supervisorStrategy = OneForOneStrategy() {
    case exception: Exception =>
      exception.printStackTrace()
      log.error(exception, exception.getMessage)
      Restart
  }

}

object AutorizacionActor {

  def props(sesionActorSupervisor: ActorRef) = Props(AutorizacionActor(sesionActorSupervisor))
}

/**
 * Realiza la validación de un token y si se está autorizado para acceder a la url
 * @author smontanez
 */
case class AutorizacionActor(sesionActorSupervisor: ActorRef) extends Actor with ActorLogging with FutureResponse {

  import context.dispatcher
  import co.com.alianza.util.json.MarshallableImplicits._
  import scalaz.std.AllInstances._
  import scalaz.Validation

  import co.com.alianza.domain.aggregates.usuarios.ValidacionesUsuario.errorValidacion
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
      resolveFutureValidation(future, (x: ResponseMessage) => x, errorValidacion, currentSender)

    case message: InvalidarToken =>
      val currentSender = sender()
      val futureInvalidarToken = usDataAdapter.invalidarTokenUsuario(message.token)
      futureInvalidarToken onComplete {
        case Failure(failure) => currentSender ! failure
        case Success(value) =>
          sesionActorSupervisor ! InvalidarSesion(message.token)
          currentSender ! ResponseMessage(OK, "El token ha sido removido")
      }

    case message: InvalidarTokenAgente =>
      val currentSender = sender()
      val futureInvalidarToken = usDataAdapter.invalidarTokenAgente(message.token)
      futureInvalidarToken onComplete {
        case Failure(failure) => currentSender ! failure
        case Success(value) =>
          sesionActorSupervisor ! InvalidarSesion(message.token)
          currentSender ! ResponseMessage(OK, "El token ha sido removido")
      }

    case message: InvalidarTokenClienteAdmin =>
      val currentSender = sender()
      val futureInvalidarToken = usDataAdapter.invalidarTokenClienteAdmin(message.token)
      futureInvalidarToken onComplete {
        case Failure(failure) => currentSender ! failure
        case Success(value) =>
          sesionActorSupervisor ! InvalidarSesion(message.token)
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
    println("2.0 LO ESTOY MANEJANDO !!");
    var util = new AesUtil(CryptoAesParameters.KEY_SIZE, CryptoAesParameters.ITERATION_COUNT)
    var decryptedToken = util.decrypt(CryptoAesParameters.SALT, CryptoAesParameters.IV, CryptoAesParameters.PASSPHRASE, token)
    Token.autorizarToken(decryptedToken) match {
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
   * Si usuarioOption tiene un valor se guarda en cache y retorna el usuario sin el campo contraseña
   * @param usuarioOption Option con el usuario
   * @param token El token
   * @return
   */
  private def guardaTokenCache(usuarioOption: Option[Usuario], token: String): Future[Option[Usuario]] = {
    println("3.0 LO ESTOY MANEJANDO !!");
    val validacionSesion: Future[Boolean] = ask(sesionActorSupervisor, ValidarSesion(token)).mapTo[Boolean]
    validacionSesion.map {
      case true =>
        println("4.0.1 LO ESTOY MANEJANDO !!"); usuarioOption.map(usuario => usuario.copy(contrasena = None))
      case false => println("4.0.2 LO ESTOY MANEJANDO !!"); None
    }
  }

  /**
   * Se valida si el recurso solicitado esta asociado al usuario
   * @return
   */
  private def validarRecurso(usuarioOpt: Option[Usuario], url: String) = {
    usuarioOpt match {
      case Some(usuario) =>
        val recursosFuturo = rDataAccessAdapter.obtenerRecursos(usuario.id.get)
        recursosFuturo.map(_.map(x => resolveMessageRecursos(usuario, x.filter(filtrarRecursos(_, url)))))
      case _ =>
        Future.successful(Validation.success(ResponseMessage(Unauthorized, TokenInvalido().msg)))
    }

  }

  /**
   * De acuerdo si la lista tiene contenido retorna un ResponseMessage
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
  private def filtrarRecursos(recurso: RecursoUsuario, url: String): Boolean =
    filtrarRecursos(recurso.urlRecurso, recurso.acceso, url)

  protected def filtrarRecursos(urlRecurso: String, acceso: Boolean, url: String) = {
    //TODO: quitar esos "ifseses"
    if (urlRecurso.equals(url)) acceso
    else if (urlRecurso.endsWith("/*")) {
      val urlC = urlRecurso.substring(0, urlRecurso.lastIndexOf("*"))
      if (urlC.equals(url + "/")) acceso
      else {
        if (url.length >= urlC.length) {
          //TODO: Whhhattt ??? if (url.endsWith("/")) "" else ""
          val ends = if (url.endsWith("/")) "" else ""
          val urlSuffix = url.substring(0, urlC.length) + ends
          if (urlSuffix.equals(urlC)) acceso
          else false
        } else false
      }

    } else false
  }

}

case class ForbiddenMessage(usuario: Usuario, filtro: Option[String], code: String)
