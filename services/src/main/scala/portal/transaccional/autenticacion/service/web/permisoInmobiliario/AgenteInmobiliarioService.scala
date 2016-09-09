package portal.transaccional.autenticacion.service.web.permisoInmobiliario

import co.com.alianza.app.CrossHeaders
import portal.transaccional.autenticacion.service.dto.PermisoRecursoDTO
import portal.transaccional.autenticacion.service.util.JsonFormatters.DomainJsonFormatters
import portal.transaccional.autenticacion.service.util.ws.CommonRESTFul
import spray.http.StatusCodes
import spray.routing.Route
import portal.transaccional.autenticacion.service.web.permisoInmobiliario.PermisoInmobiliario


import scala.concurrent.ExecutionContext

/**
 * Created by alexandra on 9/09/16.
 */
case class AgenteInmobiliarioService()(implicit val ec: ExecutionContext) extends CommonRESTFul with DomainJsonFormatters with CrossHeaders{

  val permisos = "/agente_inmobiliario/permisos"

  val route: Route = {
    path(permisos) {
      pathEndOrSingleSlash {
        consultar() ~ guardar()
      }
    }
  }

  private def guardar() = {
    post {
      entity(as[PermisoInmobiliario]){
        permisoRecurso =>
          complete(StatusCodes.OK)
      }
    }
  }


  private def consultar() = {

  }
}
