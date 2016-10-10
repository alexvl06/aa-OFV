package portal.transaccional.autenticacion.service.web.autorizacion

import co.com.alianza.app.CrossHeaders
import co.com.alianza.exceptions._
import portal.transaccional.autenticacion.service.drivers.autorizacion.AutorizacionRecursoComercialRepository
import portal.transaccional.autenticacion.service.dto.PermisoRecursoDTO
import portal.transaccional.autenticacion.service.util.JsonFormatters.DomainJsonFormatters
import portal.transaccional.autenticacion.service.util.ws.CommonRESTFul
import spray.http.StatusCodes
import spray.routing.{ Route, StandardRoute }

import scala.concurrent.ExecutionContext
import scala.util.{ Failure, Success }

/**
 * Created by dfbaratov on 23/08/16.
 */
case class AutorizacionRecursoComercialService(autorizacionRepository: AutorizacionRecursoComercialRepository)(implicit val ec: ExecutionContext) extends CommonRESTFul with DomainJsonFormatters with CrossHeaders {

  val route: Route = {
    pathPrefix("recursoComercial" / Segment) {
      recurso =>
        pathEndOrSingleSlash {
          roles(recurso)
        }
    } ~
      pathPrefix("recursoComercial") {
        pathPrefix("admin") {
          pathPrefix("recursos") {
            actualizarRecurso()
          }
        }
      }
  }

  private def roles(recurso: String) = {
    get {
      val roles = autorizacionRepository.obtenerRolesPorRecurso(recurso)
      onComplete(roles) {
        case Success(value) => complete(value)
        case Failure(ex) => execution(ex)
      }
    }
  }

  private def actualizarRecurso() = {
    post {
      entity(as[PermisoRecursoDTO]) {
        permisoRecurso =>
          val respuesta = autorizacionRepository.actualizarRecursos(permisoRecurso)
          onComplete(respuesta) {
            case Success(value) => complete(StatusCodes.OK)
            case Failure(ex) => execution(ex)
          }
      }

    }
  }

  def execution(ex: Any): StandardRoute = {
    ex match {
      case ex: PersistenceException =>
        ex.printStackTrace(); complete((StatusCodes.InternalServerError, "Error inesperado"))
      case ex: Throwable => ex.printStackTrace(); complete((StatusCodes.InternalServerError, "Error inesperado"))
    }
  }

}