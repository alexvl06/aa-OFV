package portal.transaccional.autenticacion.service.web.permisoInmobiliario

import co.com.alianza.app.CrossHeaders
import co.com.alianza.infrastructure.dto.security.UsuarioAuth
import co.com.alianza.util.json.HalPaginationUtils
import portal.transaccional.autenticacion.service.drivers.permisoAgenteInmobiliario.PermisoAgenteInmobiliarioRepository
import portal.transaccional.autenticacion.service.drivers.usuarioInmobiliario.UsuarioInmobiliarioRepository
import portal.transaccional.autenticacion.service.util.JsonFormatters.DomainJsonFormatters
import portal.transaccional.autenticacion.service.util.ws.CommonRESTFul
import spray.http.{StatusCodes, Uri}
import spray.routing.Route

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

/**
  * Expone los servicios rest relacionados con agentes inmobiliarios <br/>
  * Services: <br/>
  * <ul>
  * <li>GET /agentes-inmobiliarios -> Lista todos los agentes inmobiliarios</li>
  * <li>POST / agentes-inmobiliarios -> Crea un agente inmobiliario</li>
  * <li>GET /agentes-inmobiliarios/{usuario-agente} -> Lista el detalle de un agente inmobiliario</li>
  * <li>PUT /agentes-inmobiliarios/{usuario-agente} -> Edita el detalle un agente inmobiliario</li>
  * </ul>
  */
case class AgenteInmobiliarioService(usuarioAuth: UsuarioAuth,
                                     usuariosRepo: UsuarioInmobiliarioRepository,
                                     permisosRepo: PermisoAgenteInmobiliarioRepository)(implicit val ec: ExecutionContext)
  extends CommonRESTFul with DomainJsonFormatters with CrossHeaders with HalPaginationUtils {

  val agentesPath: String = "agentes-inmobiliarios"
  val permisos = "permisos"
  val updateByProject = "updateByProject"
  val actualizarPath3 = "updateByFid"

  val route: Route = {
    pathPrefix(agentesPath) {
      getAgenteInmobiliarioList ~ createAgenteInmobiliario ~ getAgenteInmobiliario
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
      pathEndOrSingleSlash {
        entity(as[CrearAgenteInmobiliarioRequest]) { r =>
          val idAgente: Future[Int] = usuariosRepo.createAgenteInmobiliario(
            usuarioAuth.tipoIdentificacion, usuarioAuth.identificacionUsuario,
            r.correo, r.usuario,
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
  }

  private def getAgenteInmobiliario: Route = {
    get {
      pathPrefix(Segment) { usuarioAgente =>
        pathEndOrSingleSlash {
          val agenteF: Future[Option[ConsultarAgenteInmobiliarioResponse]] = usuariosRepo.getAgenteInmobiliario(
            usuarioAuth.identificacionUsuario, usuarioAgente
          )
          onComplete(agenteF) {
            case Success(agente) => agente match {
              case Some(a) => complete(StatusCodes.OK -> a)
              case _ => complete(StatusCodes.NotFound)
            }
            case Failure(exception) => complete(StatusCodes.InternalServerError)
          }
        }
      }
    }
  }

  private def getAgenteInmobiliarioList: Route = {
    get {
      pathEndOrSingleSlash {
        requestUri { uri =>
          parameters('nombre.as[Option[String]], 'usuario.as[Option[String]],
            'correo.as[Option[String]], 'estado.as[Option[Int]], 'pagina.as[Option[Int]], 'itemsPorPagina.as[Option[Int]]) {
            (nombreOpt, usuarioOpt, correoOpt, estadoOpt, paginaOpt, itemsPorPaginaOpt) => {
              val agentesF: Future[ConsultarAgenteInmobiliarioListResponse] = usuariosRepo.getAgenteInmobiliarioList(
                usuarioAuth.identificacionUsuario, nombreOpt,
                usuarioOpt, correoOpt, estadoOpt, paginaOpt, itemsPorPaginaOpt
              )
              onComplete(agentesF) {
                case Success(agentes) =>
                  val links = getHalLinks(
                    agentes._metadata.totalItems, agentes._metadata.itemsPorPagina,
                    agentes._metadata.pagina, uri.toRelative, uri.toRelative.query.toMap
                  )
                  complete(StatusCodes.OK -> agentes.copy(_metadata = agentes._metadata.copy(links = Some(links))))
                case Failure(exception) => complete(StatusCodes.InternalServerError)
              }
            }
          }
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
