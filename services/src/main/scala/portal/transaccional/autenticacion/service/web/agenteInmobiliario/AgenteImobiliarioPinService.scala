package portal.transaccional.autenticacion.service.web.agenteInmobiliario

import co.com.alianza.app.CrossHeaders
import co.com.alianza.exceptions.{ ValidacionException, ValidacionExceptionPasswordRules }
import enumerations.EstadosPin._
import portal.transaccional.autenticacion.service.drivers.contrasenaAgenteInmobiliario.ContrasenaAgenteInmobiliarioRepository
import portal.transaccional.autenticacion.service.drivers.usuarioAgenteInmobiliario.UsuarioInmobiliarioPinRepository
import portal.transaccional.autenticacion.service.util.JsonFormatters.DomainJsonFormatters
import portal.transaccional.autenticacion.service.util.ws.CommonRESTFul
import spray.http.StatusCodes
import spray.routing.Route

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Success }

/**
 * Expone los servicios rest relacionados los pines de los agentes inmobiliarios <br/>
 * Services: <br/>
 * <ul>
 * <li>GET /agentes-inmobiliarios/pines/{pin-hash}-> Verifica la validez de un pin generado para un agente inmobiliario</li>
 * <li>PUT /agentes-inmobiliarios/pines/{pin-hash}/credenciales -> Define/actualiza la contraseña de un agente inmobiliario asociado a un pin</li>
 * </ul>
 */
case class AgenteImobiliarioPinService(
  pinRepo: UsuarioInmobiliarioPinRepository,
  agenteInmobContrasenaRepo: ContrasenaAgenteInmobiliarioRepository
)(implicit val ec: ExecutionContext)
    extends CommonRESTFul with DomainJsonFormatters with CrossHeaders {

  val agentesPath: String = "agentes-inmobiliarios"
  val credencialesPath: String = "credenciales"
  val pinesPath: String = "pines"

  val route: Route = {
    pathPrefix(agentesPath / pinesPath / Segment) { pin =>
      pathEndOrSingleSlash {
        validatePinAgente(pin)
      } ~ pathPrefix(credencialesPath) {
        pathEndOrSingleSlash {
          setCredencialesAgente(pin)
        }
      }
    }
  }

  private def validatePinAgente(pin: String): Route = {
    get {
      val validacionF: Future[Either[EstadoPin, _]] = pinRepo.validarPinAgente(pin)
      onComplete(validacionF) {
        case Success(validacion) => validacion match {
          case Right(_) => complete(StatusCodes.OK)
          case Left(estadoPin) =>
            complete(StatusCodes.Conflict)
        }
        case Failure(exception) =>
          exception.printStackTrace()
          complete(StatusCodes.InternalServerError)
      }
    }
  }

  private def setCredencialesAgente(pin: String): Route = {
    put {
      entity(as[ActualizarCredencialesAgenteRequest]) { r =>
        val actualizacionF = agenteInmobContrasenaRepo.actualizarContrasenaPin(pin, r.contrasena)
        onComplete(actualizacionF) {
          case Success(_) => complete(StatusCodes.OK)
          case Failure(error) =>
            error.printStackTrace()
            error match {
              case x: ValidacionException => complete(StatusCodes.Conflict -> x)
              case x: ValidacionExceptionPasswordRules => complete(StatusCodes.Conflict -> x)
              case _ => complete(StatusCodes.InternalServerError)
            }
        }
      }
    }
  }
}
