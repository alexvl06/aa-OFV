package portal.transaccional.autenticacion.service.web.recursoComercial

import co.com.alianza.app.CrossHeaders
import co.com.alianza.exceptions._
import portal.transaccional.autenticacion.service.drivers.rolRecursoComercial.{RecursoComercialRepository, RolComercialRepository}
import portal.transaccional.autenticacion.service.util.JsonFormatters.DomainJsonFormatters
import portal.transaccional.autenticacion.service.util.ws.CommonRESTFul
import spray.http.StatusCodes
import spray.routing.{Route, StandardRoute}

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

/**
 * Created by dfbaratov on 23/08/16.
 */

case class RecursoGraficoComercialService(recursoComercialRepository: RecursoComercialRepository, rolComercialRepository: RolComercialRepository)(implicit val ec: ExecutionContext) extends CommonRESTFul with DomainJsonFormatters with CrossHeaders {

  override def route: Route = {
    pathPrefix("recursoComercial") {
      pathPrefix("admin") {
        pathPrefix("roles"){
            roles()
        } ~
        pathPrefix("recursos"){
          recursos()
        }
      }
    }
  }

  private def roles() = {
    get {
      val roles = rolComercialRepository.obtenerTodos()
      onComplete(roles) {
        case Success(value) => complete(value)
        case Failure(ex) => execution(ex)
      }
    }
  }

  private def recursos() = {
    get {
      val recursos = recursoComercialRepository.obtenerTodosConRoles()
      onComplete(recursos) {
        case Success(value) => complete(value)
        case Failure(ex) => execution(ex)
      }
    } ~
    post {
      val recursos = recursoComercialRepository.obtenerTodosConRoles()
      onComplete(recursos) {
        case Success(value) => complete(value)
        case Failure(ex) => execution(ex)
      }
    }
  }

  def execution(ex: Any): StandardRoute = {
    ex match {
      case ex: PersistenceException =>
        ex.printStackTrace(); complete((StatusCodes.InternalServerError, "Error inesperado"))
      case ex: Throwable => ex.printStackTrace(); complete((StatusCodes.InternalServerError, "Error inesperado"))
    }
  }
}