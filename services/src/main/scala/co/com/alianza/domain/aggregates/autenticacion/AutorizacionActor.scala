package co.com.alianza.domain.aggregates.autenticacion

import java.util.Date

import akka.actor.{ActorLogging, Actor}
import co.com.alianza.constants.TiposConfiguracion
import co.com.alianza.exceptions.PersistenceException
import co.com.alianza.util.token.Token
import org.joda.time.DateTime
import scalaz.Validation
import co.com.alianza.util.json.JsonUtil
import spray.http.StatusCodes._
import co.com.alianza.infrastructure.messages.{InvalidarToken, AutorizarUrl, ResponseMessage}
import scala.Some
import co.com.alianza.infrastructure.anticorruption.usuarios.DataAccessAdapter
import co.com.alianza.infrastructure.anticorruption.recursos.{DataAccessAdapter => rDataAccessAdapter}
import scala.concurrent.Future
import co.com.alianza.util.FutureResponse
import co.com.alianza.util.transformers.ValidationT
import co.com.alianza.infrastructure.dto.{Configuracion, RecursoUsuario, Usuario}
import scalaz.std.AllInstances._
import akka.actor.Props
import akka.routing.RoundRobinPool
import scala.util.{Success, Failure}

class AutorizacionActorSupervisor extends Actor with ActorLogging {

  import akka.actor.SupervisorStrategy._
  import akka.actor.OneForOneStrategy

  val autorizacionActor = context.actorOf(Props[AutorizacionActor].withRouter(RoundRobinPool(nrOfInstances = 1)), "autorizacionActor")

  def receive = {

    case message: Any =>
      autorizacionActor forward message

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

  def receive = {
    case message: AutorizarUrl =>
      val currentSender = sender()

      val future = (for {
        tiempoSesion <- ValidationT(co.com.alianza.infrastructure.anticorruption.configuraciones.DataAccessAdapter.obtenerConfiguracionPorLlave(TiposConfiguracion.EXPIRACION_SESION.llave))
        usuarioOption <- ValidationT(validarToken(message.token, tiempoSesion))
        resultAutorizar <- ValidationT(validarRecurso(usuarioOption, message.url))
      } yield {
        resultAutorizar
      }).run

      resolveFutureValidation(future, (x: ResponseMessage) => x, currentSender)


    case message: InvalidarToken =>

      val currentSender = sender()
      val futureInvalidarToken = DataAccessAdapter.invalidarTokenUsuario(message.token)

      futureInvalidarToken onComplete {
        case Failure(failure) => currentSender ! failure
        case Success(value) => currentSender ! ResponseMessage(OK, "El token ha sido removido")
      }
  }

  /**
   * Realiza la validación del Token, llamando a [[Token.autorizarToken]]
   * Retorna un futuro con un Validationm, donde el caso de contiene el Option[Usuario]
   *
   *
   * @param token El token para realizar validación
   */
  private def validarToken(token: String, tiempoSesion: Option[Configuracion]) = {
    Token.autorizarToken(token) match {
      case true =>
        DataAccessAdapter.obtenerUsuarioToken(token).map {
          _.map { userOpt =>
            guardaTokenCache(userOpt, token, tiempoSesion.getOrElse(Configuracion(TiposConfiguracion.EXPIRACION_SESION.llave, "5")))
          }
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
  private def guardaTokenCache(usuarioOption: Option[Usuario], token: String, tiempoSesion: Configuracion): Option[Usuario] = {
    for {
      usuario <- usuarioOption
      fecha <- usuario.fechaUltimaPeticion if new DateTime(fecha.getTime).plusMinutes(tiempoSesion.valor.toInt).isAfterNow
    } yield {
      co.com.alianza.infrastructure.anticorruption.usuarios.DataAccessAdapter.actualizarFechaUltimaPeticion(usuario.identificacion, new java.sql.Timestamp(new Date().getTime))
      usuario.copy(contrasena = None)
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
