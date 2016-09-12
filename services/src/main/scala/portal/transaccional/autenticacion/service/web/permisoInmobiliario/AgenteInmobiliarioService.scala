package portal.transaccional.autenticacion.service.web.permisoInmobiliario

import co.com.alianza.app.CrossHeaders
import portal.transaccional.autenticacion.service.drivers.usuarioInmobiliario.UsuarioInmobiliarioRepository
import portal.transaccional.autenticacion.service.util.JsonFormatters.DomainJsonFormatters
import portal.transaccional.autenticacion.service.util.ws.CommonRESTFul
import spray.http.StatusCodes
import spray.routing.Route

import scala.concurrent.ExecutionContext
import scala.util.{ Failure, Success }

/**
 * Created by alexandra on 2016
 */
case class AgenteInmobiliarioService(agenteRepo: UsuarioInmobiliarioRepository)(implicit val ec: ExecutionContext) extends CommonRESTFul with
  DomainJsonFormatters with CrossHeaders {

  val permisos = "/agente_inmobiliario/permisos"

  val route: Route = {
    pathPrefix(permisos) {
      pathEndOrSingleSlash {
        consultar ~ crear
      }
    }
  }

  private def crear = {
    post {
      entity(as[EdicionPermisoRequest]) { r =>
        val resultado = agenteRepo.create(r.proyectos, r.agentesInmobiliarios, r.permisos, r.fideicomiso)
        onComplete(resultado) {
          case Success(value) => complete("Registro exitoso de permisos")
          case Failure(ex) => complete((StatusCodes.InternalServerError, "Error inesperado"))
        }
      }
    }
  }

  private def consultar = {
    get {
      path("proyectos") {
        parameters('proyecto.as[Int]) {
          (proyecto) =>
            val resultado = agenteRepo.findByProyect(proyecto)
            onComplete(resultado) {
              case Success(permisos) => complete(permisos)
              case Failure(ex) => complete((StatusCodes.InternalServerError, "Error inesperado"))
            }
        }
      }
    }
  }

  private def eliminar = ???

  private def actualizar = {
    post {
      entity(as[EdicionPermisoRequest]) { r =>
        val resultado = agenteRepo.update(r.proyectos, r.agentesInmobiliarios, r.permisos, r.fideicomiso)
        onComplete(resultado) {
          case Success(value) => complete("Actualizacion exitosa de permisos")
          case Failure(ex) => complete((StatusCodes.InternalServerError, "Error inesperado"))
        }
      }
    }
  }
}
