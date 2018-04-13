package portal.transaccional.autenticacion.service.web.menu

import co.com.alianza.app.CrossHeaders
import co.com.alianza.exceptions.{ NoAutorizado, PersistenceException, ValidacionException }
import portal.transaccional.autenticacion.service.drivers.menu._
import portal.transaccional.autenticacion.service.util.JsonFormatters.DomainJsonFormatters
import portal.transaccional.autenticacion.service.util.ws.CommonRESTFul
import spray.http.{ MediaTypes, StatusCodes }
import spray.routing.{ Route, StandardRoute }

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Success }

case class MenuService(menuUsuarioRepo: MenuUsuarioRepository)(implicit val ec: ExecutionContext) extends CommonRESTFul with DomainJsonFormatters
    with CrossHeaders {

  val menuGeneral = "menu-general"

  val route: Route = {
    pathPrefix(menuGeneral) {
      pathEndOrSingleSlash {
        obtenerMenu
      }
    }
  }

  private def obtenerMenu = {
    get {
      headerValueByName("token") {
        token =>
          respondWithMediaType(MediaTypes.`application/json`) {
            val menu: Future[MenuResponse] = menuUsuarioRepo.getMenu(token)
            onComplete(menu) {
              case Success(value) => complete(value)
              case Failure(ex) => execution(ex)
            }
          }
      }
    }
  }

  def execution(ex: Any): StandardRoute = {
    ex match {
      case ex: ValidacionException =>
        complete((StatusCodes.Unauthorized, ex))
      case ex: NoAutorizado =>
        complete((StatusCodes.Unauthorized, ex))
      case ex: PersistenceException =>
        ex.printStackTrace()
        complete((StatusCodes.InternalServerError, "Error inesperado"))
      case ex: Throwable =>
        ex.printStackTrace()
        complete((StatusCodes.InternalServerError, "Error inesperado"))
    }
  }
}
