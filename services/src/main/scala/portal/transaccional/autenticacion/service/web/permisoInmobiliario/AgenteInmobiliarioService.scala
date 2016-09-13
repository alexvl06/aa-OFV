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

  val permisos = "agenteInmobiliario"
  val actualizarPath = "updateByPerson"
  val actualizarPath2 = "updateByProject"
  val actualizarPath3 = "updateByFid"

  val route: Route = {
    pathPrefix(permisos) {
      pathEndOrSingleSlash {
        consultar ~ crear
      } ~ pathPrefix(actualizarPath) {
        pathEndOrSingleSlash {
          actualizarByPerson
        }
      } ~ pathPrefix(actualizarPath2) {
        pathEndOrSingleSlash {
          actualizarByProject
        }
      } ~ pathPrefix(actualizarPath3) {
        pathEndOrSingleSlash {
          actualizarByFid
        }
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
      parameters('proyecto.as[Int]) {
        (proyecto) =>
          val resultado = agenteRepo.findByProyect(proyecto)
          onComplete(resultado) {
            case Success(permisos) => complete(permisos)
            case Failure(ex) => println(ex); complete((StatusCodes.InternalServerError, s"Error inesperado $ex"))
          }
      }
    }
  }

  private def eliminar = ???

  private def actualizarByPerson = {
    post {
      entity(as[EdicionPermisoRequest]) { r =>
        val resultado = agenteRepo.updateByPerson(r.proyectos, r.agentesInmobiliarios, r.permisos, r.fideicomiso)
        onComplete(resultado) {
          case Success(value) => complete("Actualizacion exitosa de permisos")
          case Failure(ex) => println(ex);complete((StatusCodes.InternalServerError, "Error en la actualizacion"))
        }
      }
    }
  }

  private def actualizarByProject = {
    post {
      entity(as[EdicionPermisoRequest]) { r =>
        val resultado = agenteRepo.updateByProject(r.proyectos.head, r.agentesInmobiliarios, r.permisos, r.fideicomiso)
        onComplete(resultado) {
          case Success(value) => complete("Actualizacion exitosa de permisos")
          case Failure(ex) => println(ex.printStackTrace);complete((StatusCodes.InternalServerError, "Error en la actualizacion"))
        }
      }
    }
  }

  private def actualizarByFid = {
    post {
      entity(as[EdicionFidPermisoRequest]) { r =>
        val resultado = agenteRepo.updateByFid(r.proyectos.head, r.agentesInmobiliarios, r.fideicomiso)
        onComplete(resultado) {
          case Success(value) => complete("Actualizacion exitosa de permisos")
          case Failure(ex) => println(ex.printStackTrace);complete((StatusCodes.InternalServerError, "Error en la actualizacion"))
        }
      }
    }
  }



}
