package portal.transaccional.autenticacion.service.web.preguntasAutovalidacion

import co.com.alianza.app.CrossHeaders
import co.com.alianza.exceptions._
import portal.transaccional.autenticacion.service.drivers.preguntasAutovalidacion.PreguntasAutovalidacionRepository
import portal.transaccional.autenticacion.service.util.JsonFormatters.DomainJsonFormatters
import portal.transaccional.autenticacion.service.util.ws.CommonRESTFul
import spray.http.StatusCodes
import spray.routing._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

/**
  * Created by s4n on 3/08/16.
  */
case class PreguntasAutovalidacionService(preguntasValidacionRepository: PreguntasAutovalidacionRepository)
  (implicit val ec: ExecutionContext) extends CommonRESTFul with DomainJsonFormatters with CrossHeaders {

  val preguntasAutovalidacionPath = "preguntasAutovalidacion"

  val route: Route = {
    path(preguntasAutovalidacionPath) {
      pathEndOrSingleSlash {
        obtenerPreguntasAutovalidacion()
      }
    }
  }

  private def obtenerPreguntasAutovalidacion() = {
    get {
      val resultado: Future[PreguntasResponse] = preguntasValidacionRepository.obtenerPreguntas()
      onComplete(resultado) {
        case Success(value) => complete(value)
        case Failure(ex) => execution(ex)
      }
    }
  }

  def execution(ex: Any): StandardRoute = {
    ex match {
      case ex: ValidacionException => complete((StatusCodes.Conflict, ex))
      case ex: PersistenceException =>
        ex.printStackTrace(); complete((StatusCodes.InternalServerError, "Error inesperado"))
      case ex: Throwable => ex.printStackTrace(); complete((StatusCodes.InternalServerError, "Error inesperado"))
    }
  }


}
