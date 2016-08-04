package portal.transaccional.autenticacion.service.web.autenticacion

import akka.actor.ActorSelection
import co.com.alianza.app.CrossHeaders
import co.com.alianza.commons.enumerations.TiposCliente
import co.com.alianza.exceptions.{ PersistenceException, ValidacionException }
import co.com.alianza.infrastructure.auditing.AuditingHelper
import co.com.alianza.infrastructure.auditing.AuditingHelper.requestWithAuiditing
import portal.transaccional.autenticacion.service.drivers.autenticacion.{ AutenticacionComercialRepository, AutenticacionEmpresaRepository, AutenticacionRepository }
import portal.transaccional.autenticacion.service.util.JsonFormatters.DomainJsonFormatters
import portal.transaccional.autenticacion.service.util.ws.CommonRESTFul
import spray.http.StatusCodes
import spray.routing._

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Success }

case class AutenticacionService(
  autenticacionRepositorio: AutenticacionRepository,
    autenticacionEmpresaRepositorio: AutenticacionEmpresaRepository,
    /*autenticacionComercialRepositorio: AutenticacionComercialRepository,*/
    kafkaActor: ActorSelection
)(implicit val ec: ExecutionContext) extends CommonRESTFul with DomainJsonFormatters with CrossHeaders {

  val autenticar = "autenticar"
  val autenticarUsuarioEmpresa = "autenticarUsuarioEmpresa"
  val comercialPath = "comercial"

  val route: Route = {
    pathPrefix(autenticar) {
      pathEndOrSingleSlash {
        autenticarUsuarioIndividual
      }
    } ~
      pathPrefix(autenticarUsuarioEmpresa) {
        pathEndOrSingleSlash {
          autenticarUsuarioEmpresarial
        }
      } ~
      pathPrefix(comercialPath / autenticar) {
        pathEndOrSingleSlash {
          autenticarUsuarioEmpresarial
        }
      }
  }

  private def autenticarUsuarioIndividual = {
    post {
      entity(as[AutenticarRequest]) {
        request =>
          clientIP { ip =>
            val resultado = autenticacionRepositorio.autenticar(request.tipoIdentificacion, request.numeroIdentificacion, request.password, ip.value)
            mapRequestContext((r: RequestContext) => requestWithAuiditing(r, AuditingHelper.fiduciariaTopic, AuditingHelper.autenticacionIndex,
              ip.value, kafkaActor, request.copy(password = null))) {
              onComplete(resultado) {
                case Success(value) => complete(value)
                case Failure(ex) => execution(ex)
              }
            }
          }
      }
    }
  }

  private def autenticarUsuarioEmpresarial = {
    post {
      entity(as[AutenticarUsuarioEmpresarialRequest]) {
        request =>
          clientIP { ip =>
            val resultado: Future[String] = autenticacionEmpresaRepositorio.autenticarUsuarioEmpresa(request.nit, request.usuario, request.password, ip.value)
            mapRequestContext((r: RequestContext) => requestWithAuiditing(r, AuditingHelper.fiduciariaTopic, AuditingHelper.autenticacionIndex,
              ip.value, kafkaActor, request.copy(password = null))) {
              onComplete(resultado) {
                case Success(value) => complete(value.toString)
                case Failure(ex) => execution(ex)
              }
            }
          }
      }
    }
  }

 /* private def autenticarUsuarioComercial = {
    post {
      entity(as[AutenticarUsuarioComercialRequest]) { request =>
        clientIP { ip =>
          validate(TiposCliente.contains(request.tipoUsuario), "Invalid user type") {
            requestUri { uri =>
              val resultado: Future[String, Any] = if (request.tipoUsuario == TiposCliente.comercialAdmin.id)
                autenticacionComercialRepositorio.authenticateAdmin(request.user, request.password, ip.value)
              /*
                  repository.authenticateAdmin( request.user, request.password, ip.value ).map {
                    case scalaz.Success( response ) => StatusCodes.Created -> createBasicHalResource( uri.path.toString(), response )
                    case scalaz.Failure( error )    => StatusCodes.Unauthorized -> createBasicHalErrResource( uri.path.toString(), error )
                  }*/
              else
                autenticacionComercialRepositorio.authenticateLDAP(request.tipoUsuario, request.user, request.password, ip.value)
              /*
                  repository.authenticateLDAP( userType, request.user, request.password, ip.value ).map {
                    case scalaz.Success( response ) => StatusCodes.Created -> createBasicHalResource( uri.path.toString(), response )
                    case scalaz.Failure( error )    => StatusCodes.Unauthorized -> createBasicHalErrResource( uri.path.toString(), error )
                  }
                  */
              onComplete(resultado) {
                case Success(value) => complete(value.toString)
                case Failure(ex) => execution(ex)
              }
            }
          }
        }
      }
    }
  }*/

  def execution(ex: Any): StandardRoute = {
    ex match {
      case ex: ValidacionException => complete((StatusCodes.Unauthorized, ex))
      case ex: PersistenceException =>
        ex.printStackTrace(); complete((StatusCodes.InternalServerError, "Error inesperado"))
      case ex: Throwable => ex.printStackTrace(); complete((StatusCodes.InternalServerError, "Error inesperado"))
    }
  }

}
