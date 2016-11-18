package portal.transaccional.autenticacion.service.web.ip

import akka.actor.ActorSelection
import co.com.alianza.app.CrossHeaders
import co.com.alianza.exceptions.{ PersistenceException, ValidacionException }
import co.com.alianza.infrastructure.auditing.AuditingHelper
import co.com.alianza.infrastructure.auditing.AuditingHelper.requestWithAuiditing
import co.com.alianza.infrastructure.dto.security.UsuarioAuth
import portal.transaccional.autenticacion.service.drivers.pin.PinRepository
import portal.transaccional.autenticacion.service.util.JsonFormatters.DomainJsonFormatters
import portal.transaccional.autenticacion.service.util.ws.CommonRESTFul
import portal.transaccional.autenticacion.service.web.pin.ContrasenaUsuario
import spray.http.StatusCodes
import spray.routing._

import scala.concurrent.{ Future, ExecutionContext }
import scala.util.{ Failure, Success }

/**
 * Created by s4n on 2016
 */
case class PinService(kafkaActor: ActorSelection, pinRepo: PinRepository)(implicit val ec: ExecutionContext)
    extends CommonRESTFul with DomainJsonFormatters with CrossHeaders {

  val route: Route = {
    path("validarPin" / Segment / IntNumber) {
      (pin, funcionalidad) => validarPin(pin, funcionalidad)
    } ~ path("validarPinClienteAdmin" / Segment / IntNumber) {
      (pin, funcionalidad) => validarPinAdmin(pin, funcionalidad)
    } ~ path("validarPinAgenteEmpresarial" / Segment) {
      pin =>
        println("validar pin agente => " + pin)
        validarPinAgente(pin)
    } ~ path("cambiarPw" / Segment) {
      pin =>
        println("cambiar pw => " + pin)
        cambiarContrasena(pin)
    } ~ path("cambiarPwClienteAdmin" / Segment) {
      pin =>
        println("cambiar pw admin => " + pin)
        cambiarContrasenaAdmin(pin)
    } ~ path("cambiarPwAgenteEmpresarial" / Segment) {
      pin =>
        println("cambiar pw agente => " + pin)
        cambiarContrasenaAgente(pin)
    }
  }

  private def validarPin(pin: String, funcionalidad: Int) = {
    post {
      val resultado: Future[Boolean] = pinRepo.validarPinUsuario(pin, funcionalidad)
      onComplete(resultado) {
        case Success(value) => complete(value.toString)
        case Failure(ex) => manejarError(ex)
      }
    }
  }

  private def validarPinAdmin(pin: String, funcionalidad: Int) = {
    post {
      val resultado: Future[Boolean] = pinRepo.validarPinAdmin(pin, funcionalidad)
      onComplete(resultado) {
        case Success(value) => complete(value.toString)
        case Failure(ex) => manejarError(ex)
      }
    }
  }

  private def validarPinAgente(pin: String) = {
    post {
      val resultado: Future[Boolean] = pinRepo.validarPinAgente(pin)
      onComplete(resultado) {
        case Success(value) => complete(value.toString)
        case Failure(ex) => manejarError(ex)
      }
    }
  }

  private def cambiarContrasena(pin: String) = {
    entity(as[ContrasenaUsuario]) {
      request =>
        {
          post {
            clientIP {
              ip =>
                val ipOption: Option[String] = request.agregarIp match {
                  case Some(true) => Option(ip.value)
                  case _ => None
                }
                mapRequestContext((r: RequestContext) => requestWithAuiditing(r, AuditingHelper.fiduciariaTopic,
                  AuditingHelper.cambioContrasenaCorreoClienteIndividualIndex, ip.value, kafkaActor, request.copy(pw = ""))) {
                  val resultado: Future[Int] = pinRepo.cambioContrasenaUsuario(pin, request.pw, ipOption)
                  onComplete(resultado) {
                    case Success(value) => complete(value.toString)
                    case Failure(ex) => manejarError(ex)
                  }
                }
            }
          }
        }
    }
  }

  private def cambiarContrasenaAdmin(pin: String) = {
    entity(as[ContrasenaUsuario]) {
      request =>
        {
          post {
            clientIP {
              ip =>
                val ipOption: Option[String] = request.agregarIp match {
                  case Some(true) => Option(ip.value)
                  case _ => None
                }
                mapRequestContext((r: RequestContext) => requestWithAuiditing(r, AuditingHelper.fiduciariaTopic,
                  AuditingHelper.cambioContrasenaCorreoClienteAdministradorIndex, ip.value, kafkaActor, request.copy(pw = ""))) {
                  val resultado: Future[Int] = pinRepo.cambioContrasenaAdmin(pin, request.pw, ipOption)
                  onComplete(resultado) {
                    case Success(value) => complete(value.toString)
                    case Failure(ex) => manejarError(ex)
                  }
                }
            }
          }
        }
    }
  }

  private def cambiarContrasenaAgente(pin: String) = {
    entity(as[ContrasenaUsuario]) {
      request =>
        {
          post {
            clientIP {
              ip =>
                mapRequestContext((r: RequestContext) => requestWithAuiditing(r, AuditingHelper.fiduciariaTopic,
                  AuditingHelper.cambioContrasenaCorreoAgenteEmpresarialIndex, ip.value, kafkaActor, request.copy(pw = null))) {
                  val resultado: Future[Int] = pinRepo.cambioContrasenaAgente(pin, request.pw)
                  onComplete(resultado) {
                    case Success(value) => complete(value.toString)
                    case Failure(ex) => manejarError(ex)
                  }
                }
            }
          }
        }
    }
  }

  def manejarError(ex: Any): StandardRoute = {
    ex match {
      case ex: ValidacionException =>
        complete((StatusCodes.Conflict, ex))
      case ex: PersistenceException =>
        ex.printStackTrace()
        complete((StatusCodes.InternalServerError, "Error inesperado"))
      case ex: Throwable =>
        ex.printStackTrace()
        complete((StatusCodes.InternalServerError, "Error inesperado"))
    }
  }

}
