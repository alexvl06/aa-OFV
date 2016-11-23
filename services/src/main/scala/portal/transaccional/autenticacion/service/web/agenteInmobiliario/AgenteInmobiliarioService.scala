package portal.transaccional.autenticacion.service.web.agenteInmobiliario

import co.com.alianza.app.CrossHeaders
import co.com.alianza.exceptions.{ PersistenceException, ValidacionException, ValidacionExceptionPasswordRules }
import co.com.alianza.infrastructure.dto.security.UsuarioAuth
import co.com.alianza.persistence.entities.PermisoAgenteInmobiliario
import co.com.alianza.util.json.HalPaginationUtils
import portal.transaccional.autenticacion.service.drivers.contrasenaAgenteInmobiliario.ContrasenaAgenteInmobiliarioRepository
import portal.transaccional.autenticacion.service.drivers.permisoAgenteInmobiliario.PermisoAgenteInmobiliarioRepository
import portal.transaccional.autenticacion.service.drivers.usuarioAgenteInmobiliario.UsuarioInmobiliarioRepository
import portal.transaccional.autenticacion.service.util.JsonFormatters.DomainJsonFormatters
import portal.transaccional.autenticacion.service.util.ws.CommonRESTFul
import spray.http.StatusCodes
import spray.routing.{ Route, StandardRoute }

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Success }

// format: OFF
/**
  * Expone los servicios rest relacionados con agentes inmobiliarios <br/>
  * Servicios: <br/>
  * <ul>
  * <li> GET  /agentes-inmobiliarios                                    Lista todos los agentes inmobiliarios</li>
  * <li> POST /agentes-inmobiliarios                                    Crea un agente inmobiliario</li>
  * <li> GET  /agentes-inmobiliarios/{usuario-agente}                   Lista el detalle de un agente inmobiliario</li>
  * <li> PUT  /agentes-inmobiliarios/{usuario-agente}                   Edita el detalle un agente inmobiliario</li>
  * <li> GET  /agentes-inmobiliarios/{usuario-agente}/permisos          Lista los recursos autorizados de un agente inmobiliario</li>
  * <li> GET  /agentes-inmobiliarios/{usuario-agente}/recursos          Lista los recursos autorizados de un agente inmobiliario</li>
  * <li> PUT  /agentes-inmobiliarios/{usuario-agente}/estado            Activa o desactiva un agente inmobiliario</li>
  * <li> PUT  /agentes-inmobiliarios/{usuario-agente}/credenciales      Cambia las credenciales de un agente inmobiliario</li>
  * <li> GET  /fideicomisos/{fid-id}/proyectos/{proyecto-id}/permisos   Lista los permisos de los agentes de un proyecto asociado a un fideicomiso</li>
  * <li> PUT  /fideicomisos/{fid-id}/proyectos/{proyecto-id}/permisos   Actualiza los permisos de los agentes de un proyecto asociado a un fideicomiso</li>
  * </ul>
  */
// format: ON
case class AgenteInmobiliarioService(
  usuarioAuth: UsuarioAuth,
  usuariosRepo: UsuarioInmobiliarioRepository,
  permisosRepo: PermisoAgenteInmobiliarioRepository,
  contrasenaRepo: ContrasenaAgenteInmobiliarioRepository
)(implicit val ec: ExecutionContext)
  extends CommonRESTFul with DomainJsonFormatters with CrossHeaders with HalPaginationUtils {

  val agentesPath: String = "agentes-inmobiliarios"
  val estadoPath: String = "estado"
  val fideicomisosPath: String = "fideicomisos"
  val permisosPath = "permisos"
  val proyectosPath: String = "proyectos"
  val recursosPath = "recursos"
  val changePassPath = "credenciales"

  val route: Route = {
    pathPrefix(fideicomisosPath / IntNumber / proyectosPath / IntNumber / permisosPath) { (fideicomiso, proyecto) =>
      pathEndOrSingleSlash {
        getPermisosProyecto(fideicomiso, proyecto) ~ updatePermisosProyecto(fideicomiso, proyecto)
      }
    } ~ pathPrefix(agentesPath / Segment / permisosPath) { usuarioAgente =>
      pathEndOrSingleSlash {
        getPermisosAgente(usuarioAgente)
      }
    } ~ pathPrefix(agentesPath) {
      pathEndOrSingleSlash {
        getAgenteInmobiliarioList ~ createAgenteInmobiliario
      }
    } ~ pathPrefix(agentesPath / recursosPath) {
      pathEndOrSingleSlash {
        getRecursos
      }
    } ~ pathPrefix(agentesPath / Segment / changePassPath) { usuarioAgente =>
      pathEndOrSingleSlash {
        updateByPassword
      }
    } ~ pathPrefix(agentesPath / Segment) { usuarioAgente =>
      pathEndOrSingleSlash {
        getAgenteInmobiliario(usuarioAgente) ~ updateAgenteInmobiliario(usuarioAgente)
      }
    } ~ pathPrefix(agentesPath / Segment / estadoPath) { usuarioAgente =>
      pathEndOrSingleSlash {
        activateOrDeactivateAgenteInmobiliario(usuarioAgente)
      }
    }
  }

  private def createAgenteInmobiliario: Route = {
    post {
      entity(as[CrearAgenteInmobiliarioRequest]) { r =>
        val idAgente: Future[Int] = usuariosRepo.createAgenteInmobiliario(
          usuarioAuth.id, usuarioAuth.tipoIdentificacion, usuarioAuth.identificacion,
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

  private def getAgenteInmobiliario(usuarioAgente: String): Route = {
    get {
      val agenteF: Future[Option[ConsultarAgenteInmobiliarioResponse]] = usuariosRepo.getAgenteInmobiliario(
        usuarioAuth.identificacion, usuarioAgente
      )
      onComplete(agenteF) {
        case Success(agente: Option[ConsultarAgenteInmobiliarioResponse]) => agente match {
          case Some(a) => complete((StatusCodes.OK, a))
          case _ => complete(StatusCodes.NotFound)
        }
        case Failure(exception) => complete(StatusCodes.InternalServerError)
      }
    }
  }

  private def getAgenteInmobiliarioList: Route = {
    get {
      requestUri { uri =>
        parameters('nombre.as[Option[String]], 'usuario.as[Option[String]],
          'correo.as[Option[String]], 'estado.as[Option[String]], 'pagina.as[Option[Int]], 'itemsPorPagina.as[Option[Int]], 'ordenarPor.as[Option[String]]) {
          (nombreOpt, usuarioOpt, correoOpt, estadoOpt, paginaOpt, itemsPorPaginaOpt, ordenarPorOpt) =>
          {
            val agentesF: Future[ConsultarAgenteInmobiliarioListResponse] = usuariosRepo.getAgenteInmobiliarioList(
              usuarioAuth.identificacion, nombreOpt,
              usuarioOpt, correoOpt, estadoOpt, paginaOpt, itemsPorPaginaOpt, ordenarPorOpt
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

  private def updateAgenteInmobiliario(usuarioAgente: String): Route = {
    put {
      entity(as[CrearAgenteInmobiliarioRequest]) { r =>
        val numModificadas: Future[Int] = usuariosRepo.updateAgenteInmobiliario(
          usuarioAuth.identificacion, usuarioAgente,
          r.correo, r.nombre, r.cargo, r.descripcion
        )
        onComplete(numModificadas) {
          case Success(num) => num match {
            case 0 => complete(StatusCodes.NotFound)
            case _ => complete(StatusCodes.OK)
          }
          case Failure(exception) => complete(StatusCodes.InternalServerError)
        }
      }
    }
  }

  private def activateOrDeactivateAgenteInmobiliario(usuarioAgente: String): Route = {
    put {
      val agenteF: Future[Option[ConsultarAgenteInmobiliarioResponse]] = usuariosRepo.activateOrDeactivateAgenteInmobiliario(
        usuarioAuth.identificacion, usuarioAgente
      )
      onComplete(agenteF) {
        case Success(agenteOp) => agenteOp match {
          case Some(agente: ConsultarAgenteInmobiliarioResponse) => complete(StatusCodes.OK -> agente)
          case None => complete(StatusCodes.NotFound)
        }
        case Failure(exception) => complete(StatusCodes.InternalServerError)
      }
    }
  }

  private def getPermisosProyecto(fideicomiso: Int, proyecto: Int): Route = {
    get {
      parameters("ids" ? "") { ids =>
        val idAgentes: Seq[Int] = ids match {
          case x if x.isEmpty => Seq.empty
          case x => x.split(",").map(_.toInt).toSeq
        }
        val permisosF: Future[Seq[PermisoAgenteInmobiliario]] = permisosRepo.getPermisosProyecto(
          usuarioAuth.identificacion, fideicomiso, proyecto, idAgentes
        )
        onComplete(permisosF) {
          case Success(permisos) => complete(StatusCodes.OK -> permisos)
          case Failure(exception) => complete(StatusCodes.InternalServerError)
        }
      }
    }
  }

  private def getPermisosAgente(username : String): Route = {
    get {
      val permisosF: Future[Seq[PermisoAgenteInmobiliario]] = permisosRepo.getPermisosProyectoByAgente(usuarioAuth.id, username)
      onComplete(permisosF) {
        case Success(permisos) => complete(StatusCodes.OK -> permisos)
        case Failure(exception) => complete(StatusCodes.InternalServerError)
      }
    }
  }

  private def updatePermisosProyecto(fideicomiso: Int, proyecto: Int): Route = {
    put {
      parameters("ids" ? "") { ids =>
        entity(as[Seq[PermisoAgenteInmobiliario]]) { permisos =>
          val idAgentes: Seq[Int] = ids match {
            case x if x.isEmpty => Seq.empty
            case x => x.split(",").map(_.toInt).toSeq
          }
          val updateF: Future[Option[Int]] = permisosRepo.updatePermisosProyecto(
            usuarioAuth.identificacion, fideicomiso, proyecto, idAgentes, permisos
          )
          onComplete(updateF) {
            case Success(update) => complete(StatusCodes.OK)
            case Failure(exception) => complete(StatusCodes.InternalServerError)
          }
        }
      }
    }
  }

  private def getRecursos: Route = {
    get {
      parameters("matriz" ? false) { isMatriz =>
        val recursosF = permisosRepo.getRecurso(usuarioAuth.id, usuarioAuth.tipoCliente, isMatriz)
        onComplete(recursosF) {
          case Success(recursos) => complete(StatusCodes.OK -> recursos)
          case Failure(exception) => complete((StatusCodes.Conflict, "hubo un error"))
        }
      }
    }
  }

  private def updateByPassword: Route = {
    put {
      entity(as[ActualizarCredencialesAgenteRequest]) { contraseñas =>
        val updateF = contrasenaRepo.actualizarContrasenaCaducada(Option.empty, contraseñas.contrasenaActual.getOrElse(""), contraseñas.contrasena,
          Option(usuarioAuth.id))

        onComplete(updateF) {
          case Success(resultado) => complete(StatusCodes.OK)
          case Failure(ex) => execution(ex)
        }
      }
    }
  }

  def execution(ex: Any): StandardRoute = {
    ex match {
      case ex: ValidacionException => complete((StatusCodes.Conflict, ex))
      case ex: ValidacionExceptionPasswordRules => complete((StatusCodes.Conflict, ex))
      case ex: PersistenceException => complete((StatusCodes.InternalServerError, "Error inesperado"))
      case ex: Throwable => complete((StatusCodes.InternalServerError, "Error inesperado"))
    }
  }
}
