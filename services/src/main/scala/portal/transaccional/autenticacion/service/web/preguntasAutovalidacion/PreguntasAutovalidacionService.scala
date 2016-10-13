package portal.transaccional.autenticacion.service.web.preguntasAutovalidacion

import co.com.alianza.app.CrossHeaders
import co.com.alianza.commons.enumerations.TiposCliente
import co.com.alianza.commons.enumerations.TiposCliente._
import co.com.alianza.exceptions._
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
case class PreguntasAutovalidacionService(
  user: UsuarioAuth,
  preguntasAutoValidacionRepository: PreguntasAutovalidacionRepository,
  respuestaUsuarioRepository: RespuestaUsuarioRepository,
  respuestaUsuarioAdminRepository: RespuestaUsuarioRepository
)(implicit val ec: ExecutionContext)
    extends CommonRESTFul with DomainJsonFormatters with CrossHeaders {

  val preguntasAutovalidacionPath = "preguntasAutovalidacion"
  val comprobarPath = "comprobar"

  val route: Route = {
    path(preguntasAutovalidacionPath) {
      pathEndOrSingleSlash {
        obtenerPreguntasAutovalidacion() ~
          guardarRespuestas(user)
      }
    } ~ path(preguntasAutovalidacionPath / comprobarPath) {
      pathEndOrSingleSlash {
        obtenerPreguntasComprobar()
      }
    }
  }

  private def obtenerPreguntasAutovalidacion() = {
    get {
      val resultado: Future[ResponseObtenerPreguntas] = preguntasAutoValidacionRepository.obtenerPreguntas()
      onComplete(resultado) {
        case Success(value) => complete(value)
        case Failure(ex) => execution(ex)
      }
    }
  }

  private def guardarRespuestas(user: UsuarioAuth) = {
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

  private def obtenerPreguntasComprobar() = {
    get {
      val resultado: Future[ResponseObtenerPreguntasComprobar] =
        preguntasAutoValidacionRepository.obtenerPreguntasComprobar(user.id, user.tipoCliente)
      onComplete(resultado) {
        case Success(value) => complete(value)
        case Failure(ex) => execution(ex)
      }
    } ~ post {
      entity(as[RespuestasComprobacionRequest]) {
        request =>
          clientIP {
            ip =>
              // TODO: AUDITORIA by:Jonathan
              val resultado: Future[String] = preguntasAutoValidacionRepository.validarRespuestas(user.id, user.tipoCliente, request.respuestas, request.numeroIntentos)
              onComplete(resultado) {
                case Success(value) => complete(value)
                case Failure(ex) => execution(ex)
              }
          }
      }
    } ~ delete {
      clientIP {
        ip =>
          // TODO: AUDITORIA by:Jonathan
          val resultado: Future[Int] = preguntasAutoValidacionRepository.bloquearRespuestas(user.id: Int, user.tipoCliente: TiposCliente)
          onComplete(resultado) {
            case Success(value) => complete(value.toString)
            case Failure(ex) => execution(ex)
          }
      }
    }
  }

  def execution(ex: Any): StandardRoute = {
    ex match {
      case ex: ValidacionException => complete((StatusCodes.Conflict, ex.data))
      case ex: PersistenceException =>
        ex.printStackTrace(); complete((StatusCodes.InternalServerError, "Error inesperado"))
      case ex: Throwable => ex.printStackTrace(); complete((StatusCodes.InternalServerError, "Error inesperado"))
    }
  }
}

/*
*
* package co.com.alianza.web

import akka.actor.{ ActorSelection, ActorSystem }
import co.com.alianza.app.{ AlianzaCommons, CrossHeaders }
import co.com.alianza.commons.enumerations.TiposCliente
import co.com.alianza.infrastructure.auditing.AuditingHelper
import co.com.alianza.infrastructure.auditing.AuditingHelper._
import co.com.alianza.infrastructure.dto.security.UsuarioAuth
import co.com.alianza.infrastructure.messages._
import spray.http.StatusCodes
import spray.routing.{ Directives, RequestContext }

import scala.concurrent.ExecutionContext

/**
 *
 * @author seven4n
 */
case class PreguntasAutovalidacionService(kafkaActor: ActorSelection, preguntasAutovalidacionActor: ActorSelection)(implicit val system: ActorSystem)
    extends Directives with AlianzaCommons with CrossHeaders {

  import co.com.alianza.infrastructure.messages.PreguntasAutovalidacionMessagesJsonSupport._

  def route(user: UsuarioAuth) = {
    pathPrefix("preguntasAutovalidacion") {
      if (user.tipoCliente.eq(TiposCliente.comercialSAC))
        complete((StatusCodes.Unauthorized, "Tipo usuario SAC no está autorizado para realizar esta acción"))
      else
        get {
          respondWithMediaType(mediaType) {
            pathPrefix("comprobar") {
              requestExecute(new ObtenerPreguntasComprobarMessage(user.id, user.tipoCliente), preguntasAutovalidacionActor)
            } ~ {
              requestExecute(new ObtenerPreguntasMessage, preguntasAutovalidacionActor)
            }
          }
        } ~ put {
          respondWithMediaType(mediaType) {
            {
              entity(as[RespuestasMessage]) {
                message: RespuestasMessage =>
                  val guardarMessage = GuardarRespuestasMessage(user.id, user.tipoCliente, message.respuestas)
                  requestExecute(guardarMessage, preguntasAutovalidacionActor)
              }
            }
          }
        } ~ post {
          respondWithMediaType(mediaType) {
            pathPrefix("comprobar") {
              entity(as[RespuestasComprobacionMessage]) {
                message: RespuestasComprobacionMessage =>
                  clientIP {
                    ip =>
                      val validacionMessage = ValidarRespuestasMessage(user.id, user.tipoCliente, message.respuestas, message.numeroIntentos)
                      mapRequestContext((r: RequestContext) => requestWithAuiditing(r, AuditingHelper.fiduciariaTopic,
                        AuditingHelper.autovalidacionComprobarIndex, ip.value, kafkaActor, message)) {
                        requestExecute(validacionMessage, preguntasAutovalidacionActor)
                      }
                  }
              }
            }
          }
        } ~ delete {
          respondWithMediaType(mediaType) {
            pathPrefix("comprobar") {
              clientIP {
                ip =>
                  val message = new BloquearRespuestasMessage(user.id, user.tipoCliente)
                  mapRequestContext((r: RequestContext) => requestWithAuiditing(r, AuditingHelper.fiduciariaTopic,
                    AuditingHelper.autovalidacionBloquearIndex, ip.value, kafkaActor, message)) {
                    requestExecute(message, preguntasAutovalidacionActor)
                  }
              }
            }
          }
        }
    }
  }
}

* */ 