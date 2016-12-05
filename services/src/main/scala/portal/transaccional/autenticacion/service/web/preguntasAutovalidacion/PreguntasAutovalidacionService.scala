package portal.transaccional.autenticacion.service.web.preguntasAutovalidacion

import akka.actor.ActorSelection
import co.com.alianza.app.CrossHeaders
import co.com.alianza.commons.enumerations.TiposCliente
import co.com.alianza.exceptions._
import co.com.alianza.infrastructure.auditing.AuditingHelper
import co.com.alianza.infrastructure.auditing.AuditingHelper._
import co.com.alianza.infrastructure.auditing.AuditingUser.AuditingUserData
import co.com.alianza.infrastructure.dto.security.UsuarioAuth
import portal.transaccional.autenticacion.service.drivers.pregunta.PreguntasAutovalidacionRepository
import portal.transaccional.autenticacion.service.drivers.respuesta.RespuestaUsuarioRepository
import portal.transaccional.autenticacion.service.util.JsonFormatters.DomainJsonFormatters
import portal.transaccional.autenticacion.service.util.ws.CommonRESTFul
import spray.http.StatusCodes
import spray.routing._

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Success }

/**
 * Created by s4n on 3/08/16.
 */
case class PreguntasAutovalidacionService(user: UsuarioAuth, kafkaActor: ActorSelection, preguntasAutoValidacionRepository: PreguntasAutovalidacionRepository,
  respuestaUsuarioRepository: RespuestaUsuarioRepository, respuestaUsuarioAdminRepository: RespuestaUsuarioRepository)(implicit val ec: ExecutionContext)
    extends CommonRESTFul with DomainJsonFormatters with CrossHeaders {

  val preguntasAutovalidacionPath = "preguntasAutovalidacion"
  val comprobarPath = "comprobar"

  val route: Route = {
    path(preguntasAutovalidacionPath) {
      pathEndOrSingleSlash {
        obtenerPreguntasAutovalidacion ~ guardarRespuestas
      }
    } ~ path(preguntasAutovalidacionPath / comprobarPath) {
      pathEndOrSingleSlash {
        obtenerPreguntas ~ comprobar ~ bloquear
      }
    }
  }

  private def obtenerPreguntasAutovalidacion = {
    get {
      val resultado: Future[ResponseObtenerPreguntas] = preguntasAutoValidacionRepository.obtenerPreguntas()
      onComplete(resultado) {
        case Success(value) => complete(value)
        case Failure(ex) => execution(ex)
      }
    }
  }

  private def guardarRespuestas = {
    put {
      entity(as[GuardarRespuestasRequest]) {
        request =>
          val resultado: Future[Option[Int]] = user.tipoCliente match {
            case TiposCliente.clienteIndividual =>
              respuestaUsuarioRepository.guardarRespuestas(user.id, request.respuestas)
            case TiposCliente.clienteAdministrador =>
              respuestaUsuarioAdminRepository.guardarRespuestas(user.id, request.respuestas)
          }
          onComplete(resultado) {
            case Success(value) => complete(value.toString)
            case Failure(ex) => execution(ex)
          }
      }
    }
  }

  private def obtenerPreguntas = {
    get {
      val resultado: Future[ResponseObtenerPreguntasComprobar] =
        preguntasAutoValidacionRepository.obtenerPreguntasComprobar(user.id, user.tipoCliente)
      onComplete(resultado) {
        case Success(value) => complete(value)
        case Failure(ex) => execution(ex)
      }
    }
  }

  private def comprobar = {
    post {
      entity(as[RespuestasComprobacionRequest]) {
        request =>
          clientIP {
            ip =>
              val usuario: Option[AuditingUserData] = getAuditingUser(user.tipoIdentificacion, user.identificacion, user.usuario)
              mapRequestContext((r: RequestContext) =>
                requestAuditing[PersistenceException, RespuestasComprobacionRequest](r, AuditingHelper.fiduciariaTopic,
                  AuditingHelper.autovalidacionComprobarIndex, ip.value, kafkaActor, usuario, Some(request))) {
                val resultado: Future[String] = preguntasAutoValidacionRepository.validarRespuestas(user, request.respuestas, request.numeroIntentos)
                onComplete(resultado) {
                  case Success(value) => complete(value)
                  case Failure(ex) => execution(ex)
                }
              }
          }
      }
    }
  }

  private def bloquear = {
    delete {
      clientIP {
        ip =>
          val usuario: Option[AuditingUserData] = getAuditingUser(user.tipoIdentificacion, user.identificacion, user.usuario)
          mapRequestContext((r: RequestContext) =>
            requestAuditing(r, AuditingHelper.fiduciariaTopic, AuditingHelper.autovalidacionBloquearIndex, ip.value, kafkaActor, usuario, None)) {
            val resultado: Future[Int] = preguntasAutoValidacionRepository.bloquearRespuestas(user.id: Int, user.tipoCliente)
            onComplete(resultado) {
              case Success(value) => complete(value.toString)
              case Failure(ex) => execution(ex)
            }
          }
      }
    }
  }

  def execution(ex: Throwable): StandardRoute = {
    ex match {
      case ex: ValidacionException => complete((StatusCodes.Conflict, ex.data))
      case ex: PersistenceException =>
        ex.printStackTrace(); complete((StatusCodes.InternalServerError, "Error inesperado"))
      case ex: Throwable => ex.printStackTrace(); complete((StatusCodes.InternalServerError, "Error inesperado"))
    }
  }

}
