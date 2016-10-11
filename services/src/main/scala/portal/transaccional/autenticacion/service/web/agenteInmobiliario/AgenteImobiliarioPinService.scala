package portal.transaccional.autenticacion.service.web.agenteInmobiliario

import co.com.alianza.app.CrossHeaders
import enumerations.EstadosPin._
import portal.transaccional.autenticacion.service.drivers.usuarioAgenteInmobiliario.UsuarioInmobiliarioPinRepository
import portal.transaccional.autenticacion.service.util.JsonFormatters.DomainJsonFormatters
import portal.transaccional.autenticacion.service.util.ws.CommonRESTFul
import spray.http.StatusCodes
import spray.routing.Route

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

/**
  * Expone los servicios rest relacionados los pines de los agentes inmobiliarios <br/>
  * Services: <br/>
  * <ul>
  * <li>PUT /agentes-inmobiliarios/pines/{pin-hash}-> Verifica la validez de un pin generado para un agente inmobiliario</li>
  * </ul>
  */
case class AgenteImobiliarioPinService(pinRepo: UsuarioInmobiliarioPinRepository)(implicit val ec: ExecutionContext)
  extends CommonRESTFul with DomainJsonFormatters with CrossHeaders {

  val agentesPath: String = "agentes-inmobiliarios"
  val pinesPath: String = "pines"

  val route: Route = {
    pathPrefix(agentesPath / pinesPath / Segment) { pin =>
      pathEndOrSingleSlash {
        validatePinAgente(pin)
      }
    }
  }

  private def validatePinAgente(pin: String): Route = {
    get {
      val validacionF: Future[Either[EstadoPin, _]] = pinRepo.validarPinAgente(pin)
      onComplete(validacionF) {
        case Success(validacion) => validacion match {
          case Right(_) => complete(StatusCodes.OK)
          case Left(estadoPin) => complete(StatusCodes.Conflict)
        }
        case Failure(exception) => complete(StatusCodes.InternalServerError)
      }
    }
  }
}
