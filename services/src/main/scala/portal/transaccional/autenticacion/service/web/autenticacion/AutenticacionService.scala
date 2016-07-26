package portal.transaccional.autenticacion.service.web.autenticacion

import akka.actor.{ ActorRef, ActorSelection }
import co.com.alianza.exceptions.{ PersistenceException, ValidacionException }
import portal.transaccional.autenticacion.service.drivers.autenticacion.AutenticacionRepository
import portal.transaccional.autenticacion.service.util.JsonFormatters.DomainJsonFormatters
import portal.transaccional.autenticacion.service.util.ws.CommonRESTFul
import spray.http.StatusCodes
import spray.routing._
import co.com.alianza.app.CrossHeaders
import co.com.alianza.infrastructure.auditing.AuditingHelper
import co.com.alianza.infrastructure.auditing.AuditingHelper.requestWithAuiditing

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Success }

case class AutenticacionService(
    autenticacionRepositorio: AutenticacionRepository, kafkaActor: ActorSelection
)(implicit val ec: ExecutionContext) extends CommonRESTFul with DomainJsonFormatters with CrossHeaders {

  val autenticar = "autenticar"
  val autenticarUsuarioEmpresa = "autenticarUsuarioEmpresa"

  val route: Route = {
    pathPrefix(autenticar) {
      pathEndOrSingleSlash {
        autenticarUsuarioIndividual ~ autenticarUsuarioEmpresarial
      }
    }
  }

  private def autenticarUsuarioIndividual = {
    post {
      entity(as[AutenticarRequest]) {
        autenticacionRequest =>
          clientIP { ip =>
            val request = autenticacionRequest.copy(clientIp = ip.value)
            val resultado: Future[String] =
              autenticacionRepositorio.autenticar(request.tipoIdentificacion, request.numeroIdentificacion, request.password, request.clientIp)
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

  private def autenticarUsuarioEmpresarial = {
    path(autenticarUsuarioEmpresa) {
      post {
        entity(as[AutenticarUsuarioEmpresarialRequest]) {
          autenticacionRequest =>
            clientIP { ip =>
              val request = autenticacionRequest.copy(clientIp = ip.value)
              val resultado: Future[String] =
                autenticacionRepositorio.autenticarUsuarioEmpresa(request.tipoIdentificacion, request.numeroIdentificacion, request.usuario, request.password, request.clientIp)
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
  }

  def execution(ex: Any): StandardRoute = {
    ex match {
      case ex: ValidacionException => complete((StatusCodes.InternalServerError, ex))
      case ex: PersistenceException => complete((StatusCodes.InternalServerError, "Error inesperado"))
      case ex: Throwable => complete((StatusCodes.InternalServerError, "Error inesperado"))
    }
  }

}
