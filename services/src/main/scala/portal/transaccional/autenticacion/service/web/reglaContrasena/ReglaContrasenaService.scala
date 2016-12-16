package portal.transaccional.autenticacion.service.web.reglaContrasena

import co.com.alianza.app.CrossHeaders
import co.com.alianza.persistence.entities.ReglaContrasena
import portal.transaccional.autenticacion.service.drivers.reglas.ReglaContrasenaRepository
import portal.transaccional.autenticacion.service.util.JsonFormatters.DomainJsonFormatters
import portal.transaccional.autenticacion.service.util.ws.CommonRESTFul
import spray.http.StatusCodes
import spray.routing.Route

import scala.concurrent.{ Future, ExecutionContext }
import scala.util.{ Failure, Success }

case class ReglaContrasenaService(reglasRepo: ReglaContrasenaRepository)(implicit val ec: ExecutionContext)
    extends CommonRESTFul with DomainJsonFormatters with CrossHeaders {

  val reglasContrasenas = "reglasContrasenas"

  val route: Route = {
    path(reglasContrasenas) {
      pathEndOrSingleSlash {
        reglas()
      }
    }
  }

  private def reglas(): Route = {
    get {
      onComplete(reglasRepo.getReglas()) {
        case Success(value) =>
          complete(value)
        case Failure(ex) => complete(StatusCodes.NoContent)
      }
    }
  }

}

