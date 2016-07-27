package portal.transaccional.autenticacion.service.web.autenticacion

import akka.actor.{ ActorSelection }
import co.com.alianza.exceptions.{ PersistenceException, ValidacionException }
import portal.transaccional.autenticacion.service.util.JsonFormatters.DomainJsonFormatters
import portal.transaccional.autenticacion.service.util.ws.CommonRESTFul
import spray.http.StatusCodes
import spray.routing._
import co.com.alianza.app.CrossHeaders
import co.com.alianza.infrastructure.auditing.AuditingHelper
import co.com.alianza.infrastructure.auditing.AuditingHelper.requestWithAuiditing
import portal.transaccional.autenticacion.service.drivers.autenticacion.{ AutenticacionEmpresaRepository, AutenticacionRepository }
import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Success }

case class AutenticacionService(autenticacionRepositorio: AutenticacionRepository, autenticacionEmpresaRepositorio: AutenticacionEmpresaRepository,
    kafkaActor: ActorSelection)(implicit val ec: ExecutionContext) extends CommonRESTFul with DomainJsonFormatters with CrossHeaders {

  val autenticar = "autenticar"
  val autenticarUsuarioEmpresa = "autenticarUsuarioEmpresa"

  val route: Route = {
    pathPrefix(autenticar) {
      pathEndOrSingleSlash {
        autenticarUsuarioIndividual // ~ autenticarUsuarioEmpresarial
      }
    }
  }

  private def autenticarUsuarioIndividual = {
    post {
      entity(as[AutenticarRequest]) {
        request =>
          clientIP { ip =>
            val resultado: Future[String] =
              autenticacionRepositorio.autenticar(request.tipoIdentificacion, request.numeroIdentificacion, request.password, ip.value)
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

  /* private def autenticarUsuarioEmpresarial = {
    path(autenticarUsuarioEmpresa) {
      post {
        entity(as[AutenticarUsuarioEmpresarialRequest]) {
          autenticacionRequest =>
            clientIP { ip =>
              val request = autenticacionRequest.copy(clientIp = ip.value)
              val resultado: Future[String] =
                autenticacionRepositorio.(request.tipoIdentificacion, request.numeroIdentificacion, request.usuario, request.password, request.clientIp)
              mapRequestContext((r: RequestContext) => requestWithAuiditing(r, AuditingHelper.fiduciariaTopic, AuditingHelper.autenticacionIndex, ip.value, kafkaActor, autenticacionRequest.copy(password = null, clientIp = ip.value))) {
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
      case ex: PersistenceException => complete((StatusCodes.InternalServerError, "Error inesperado"))
      case ex: Throwable => complete((StatusCodes.InternalServerError, "Error inesperado"))
    }
  }

}
