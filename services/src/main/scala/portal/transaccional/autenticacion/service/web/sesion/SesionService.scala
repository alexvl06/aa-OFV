package portal.transaccional.autenticacion.service.web.sesion

import co.com.alianza.app.CrossHeaders
import portal.transaccional.autenticacion.service.util.JsonFormatters.DomainJsonFormatters
import portal.transaccional.autenticacion.service.util.ws.CommonRESTFul
import spray.http.StatusCodes
import spray.routing._

import scala.concurrent.ExecutionContext

/**
 * Created by alexandra on 2016
 */
case class SesionService ()(implicit val ec: ExecutionContext) extends CommonRESTFul with DomainJsonFormatters with CrossHeaders {

  val actualizarSesion = "actualizarSesion"

  val route: Route = {
    path(actualizarSesion) {
      pathEndOrSingleSlash {
        actualizar()
      }
    }
  }

  // Al realizar una petición al back, ya la sesion se actualiza, por medio del [[SesionActor.scala]]
  private def actualizar() = {
    get {
      complete(StatusCodes.OK)
    }
  }

}



