package portal.transaccional.autenticacion.service.web.permisoInmobiliario

import co.com.alianza.app.CrossHeaders
import co.com.alianza.infrastructure.dto.security.UsuarioAuth
import portal.transaccional.autenticacion.service.drivers.permisoAgenteInmobiliario.PermisoAgenteInmobiliarioRepository
import portal.transaccional.autenticacion.service.drivers.usuarioInmobiliario.UsuarioInmobiliarioRepository
import portal.transaccional.autenticacion.service.util.JsonFormatters.DomainJsonFormatters
import portal.transaccional.autenticacion.service.util.ws.CommonRESTFul
import spray.http.StatusCodes
import spray.routing.Route

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

/**
  * Expone los servicios rest relacionados con agentes inmobiliarios <br/>
  * Services: <br/>
  * <ul>
  * <li>GET /agentes-inmobiliarios -> Lista todos los agentes inmobiliarios</li>
  * <li>POST / agentes-inmobiliarios -> Crea un agente inmobiliario</li>
  * <li>GET /agentes-inmobiliarios/{id-agente} -> Lista el detalle de un agente inmobiliario</li>
  * <li>PUT /agentes-inmobiliarios/{id-agente} -> Edita el detalle un agente inmobiliario</li>
  * </ul>
  */
case class AgenteInmobiliarioService(usuarioAuth: UsuarioAuth,
                                     usuariosRepo: UsuarioInmobiliarioRepository,
                                     permisosRepo: PermisoAgenteInmobiliarioRepository)(implicit val ec: ExecutionContext)
  extends CommonRESTFul with DomainJsonFormatters with CrossHeaders {

  val agentesPath: String = "agentes-inmobiliarios"
  val permisos = "permisos"
  val updateByProject = "updateByProject"
  val actualizarPath3 = "updateByFid"

  val route: Route = {
    pathPrefix(agentesPath) {
      createAgenteInmobiliario
    }
    //    pathPrefix(permisos) {
    //      pathEndOrSingleSlash {
    //        consultar ~ crear
    //      } ~ pathPrefix(updateByProject) {
    //        pathEndOrSingleSlash {
    //          actualizarByProject
    //        }
    //      } ~ pathPrefix(actualizarPath3) {
    //        pathEndOrSingleSlash {
    //          actualizarByFid
    //        }
    //      }
    //    }
  }

  private def createAgenteInmobiliario: Route = {
    post {
      entity(as[CrearAgenteInmobiliarioRequest]) { r =>
        val idAgente: Future[Int] = usuariosRepo.createAgenteInmobiliario(
          usuarioAuth.tipoIdentificacion, usuarioAuth.identificacionUsuario,
          r.usuario, r.correo,
          r.nombre, r.cargo, r.descripcion
        )
        onComplete(idAgente) {
          case Success(id) => id match {
            case 0 => complete(StatusCodes.Conflict)
            case _ => complete(StatusCodes.Created)
          }
          case Failure(exception) => complete(StatusCodes.InternalServerError)
        }
      }
    }
  }

  private def crear = {
    post {
      entity(as[EdicionPermisoRequest]) { r =>
        val resultado = permisosRepo.create(r.proyectos, r.agentesInmobiliarios, r.permisos, r.fideicomiso)
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
          val resultado = permisosRepo.findByProyect(proyecto)
          onComplete(resultado) {
            case Success(permisos) => complete(permisos)
            case Failure(ex) => println(ex); complete((StatusCodes.InternalServerError, s"Error inesperado $ex"))
          }
      }
    }
  }

  private def actualizarByProject = {
    post {
      entity(as[EdicionPermisoRequest]) { r =>
        val resultado = permisosRepo.updateByProject(r.proyectos.head, r.agentesInmobiliarios, r.permisos, r.fideicomiso)
        onComplete(resultado) {
          case Success(value) => complete("Actualizacion exitosa de permisos")
          case Failure(ex) => println(ex.printStackTrace); complete((StatusCodes.InternalServerError, "Error en la actualizacion"))
        }
      }
    }
  }

  private def actualizarByFid = {
    post {
      entity(as[EdicionFidPermisoRequest]) { r =>
        val resultado = permisosRepo.updateByFid(r.proyectos.head, r.agentesInmobiliarios, r.fideicomiso)
        onComplete(resultado) {
          case Success(value) => complete("Actualizacion exitosa de permisos")
          case Failure(ex) => println(ex.printStackTrace); complete((StatusCodes.InternalServerError, "Error en la actualizacion"))
        }
      }
    }
  }
}
