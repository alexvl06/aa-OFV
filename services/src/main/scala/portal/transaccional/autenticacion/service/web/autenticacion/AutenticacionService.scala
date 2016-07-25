package portal.transaccional.autenticacion.service.web.autenticacion

import co.com.alianza.exceptions.{ PersistenceException, ValidacionException }
import portal.transaccional.autenticacion.service.drivers.autenticacion.AutenticacionRepository
import portal.transaccional.autenticacion.service.util.JsonFormatters.DomainJsonFormatters
import portal.transaccional.autenticacion.service.util.ws.CommonRESTFul
import spray.http.StatusCodes
import spray.routing._
import co.com.alianza.app.{ AlianzaActors, CrossHeaders }
import co.com.alianza.infrastructure.auditing.AuditingHelper
import co.com.alianza.infrastructure.auditing.AuditingHelper.requestWithAuiditing

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Success }

class AutenticacionService(autenticacionRepositorio: AutenticacionRepository)(implicit val ec: ExecutionContext) extends CommonRESTFul with AlianzaActors
    with DomainJsonFormatters with CrossHeaders {

  val autenticar = "autenticar"

  val route: Route = {
    pathPrefix(autenticar) {
      pathEndOrSingleSlash {
        autenticarUsuarioIndividual
      }
    }
  }

  private def autenticarUsuarioIndividual = {
    post {
      entity(as[AutenticacionRequest]) {
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

  def execution(ex: Any): StandardRoute = {
    ex match {
      case ex: ValidacionException => complete((StatusCodes.InternalServerError, ex))
      case ex: PersistenceException => complete((StatusCodes.InternalServerError, "Error inesperado"))
      case ex: Throwable => complete((StatusCodes.InternalServerError, "Error inesperado"))
    }
  }

}
