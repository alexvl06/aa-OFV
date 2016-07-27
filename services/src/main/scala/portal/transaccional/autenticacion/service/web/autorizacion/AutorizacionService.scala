package portal.transaccional.autenticacion.service.web.autorizacion

import co.com.alianza.app.CrossHeaders
import co.com.alianza.exceptions.{ PersistenceException, ValidacionException }
import portal.transaccional.autenticacion.service.drivers.usuario.UsuarioRepository
import portal.transaccional.autenticacion.service.util.JsonFormatters.DomainJsonFormatters
import portal.transaccional.autenticacion.service.util.ws.CommonRESTFul
import spray.http.StatusCodes
import spray.routing.{ Route, StandardRoute }
import scala.util.{ Failure, Success }
import scala.concurrent.{ ExecutionContext, Future }

/**
 * Created by jonathan on 27/07/16.
 */
case class AutorizacionService(usuarioRepository: UsuarioRepository)(implicit val ec: ExecutionContext) extends CommonRESTFul with DomainJsonFormatters
    with CrossHeaders {

  val invalidarTokenPath = "invalidarToken"

  val route: Route = {
    pathPrefix(invalidarTokenPath) {
      pathEndOrSingleSlash {
        invalidarToken
      }
    }
  }

  private def invalidarToken = {
    entity(as[InvalidarTokenRequest]) {
      token =>
        delete {
          clientIP { ip =>
            val resultado: Future[Int] = usuarioRepository.invalidarToken(token.token)
            onComplete(resultado) {
              case Success(value: Int) => complete(value.toString)
              case Failure(ex) => execution(ex)
            }
          }
        }
    }
  }

  def execution(ex: Any): StandardRoute = {
    ex match {
      case ex: ValidacionException => complete((StatusCodes.Conflict, ex))
      case ex: PersistenceException => complete((StatusCodes.InternalServerError, "Error inesperado"))
      case ex: Throwable => complete((StatusCodes.InternalServerError, "Error inesperado"))
    }
  }

}
