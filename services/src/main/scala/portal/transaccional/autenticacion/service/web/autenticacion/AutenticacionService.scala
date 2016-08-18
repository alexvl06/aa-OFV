package portal.transaccional.autenticacion.service.web.autenticacion

import akka.actor.ActorSelection
import co.com.alianza.app.CrossHeaders
import co.com.alianza.commons.enumerations.TiposCliente
import co.com.alianza.exceptions.{ PersistenceException, ValidacionException }
import co.com.alianza.infrastructure.auditing.AuditingHelper
import co.com.alianza.infrastructure.auditing.AuditingHelper.requestWithAuiditing
import co.com.alianza.util.token.AesUtil
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
    autenticacionComercialRepositorio: AutenticacionComercialRepository,
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
          autenticarUsuarioComercial
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
                case Success(token) => encriptarToken(token)
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
                case Success(token) => encriptarToken(token)
                case Failure(ex) => execution(ex)
              }
            }
          }
      }
    }
  }

  private def autenticarUsuarioComercial = {
    post {
      entity(as[AutenticarUsuarioComercialRequest]) {
        request =>
          clientIP { ip =>
            val resultado: Future[String] = autenticacionComercialRepositorio.autenticar(request.usuario, request.tipoUsuario, request.contrasena, ip.value)
            onComplete(resultado) {
              case Success(token) => encriptarToken(token)
              case Failure(ex) => execution(ex)
            }
          }
      }
    }
  }

  def encriptarToken(token: String): StandardRoute = {
    complete((StatusCodes.OK, AesUtil.encriptarToken(token)))
  }

  def execution(ex: Any): StandardRoute = {
    ex match {
      case ex: ValidacionException => complete((StatusCodes.Unauthorized, ex))
      case ex: PersistenceException => print(ex); complete((StatusCodes.InternalServerError, "Error inesperado"))
      case ex: Throwable => print(ex); complete((StatusCodes.InternalServerError, "Error inesperado"))
    }
  }

}
