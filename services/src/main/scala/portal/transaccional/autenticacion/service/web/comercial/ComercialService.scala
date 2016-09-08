package portal.transaccional.autenticacion.service.web.comercial

import co.com.alianza.app.CrossHeaders
import co.com.alianza.exceptions.{ PersistenceException, ValidacionException }
import co.com.alianza.infrastructure.dto.security.UsuarioAuth
import portal.transaccional.autenticacion.service.drivers.usuarioComercialAdmin.UsuarioComercialAdminRepository
import portal.transaccional.autenticacion.service.util.JsonFormatters.DomainJsonFormatters
import portal.transaccional.autenticacion.service.util.ws.CommonRESTFul
import spray.http.StatusCodes
import spray.routing.{ Route, StandardRoute }

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Success }

case class ComercialService(user: UsuarioAuth, comercialRepo: UsuarioComercialAdminRepository)(implicit val ec: ExecutionContext) extends CommonRESTFul with DomainJsonFormatters
    with CrossHeaders {

  val comercialPath = "comercial"
  val administradorPath = "administrador"

  val route: Route = {
    pathPrefix(comercialPath / administradorPath) {
      path("crear") {
        pathEndOrSingleSlash {
          crearAdministrador()
        }
      } ~ path("contrasena") {
        pathEndOrSingleSlash {
          crearAdministrador()
        }
      }
    }
  }

  private def crearAdministrador() = {
    post {
      entity(as[CrearAdministradorRequest]) {
        request =>
          val usuario: String = request.usuario.toString
          val resultado: Future[Int] = comercialRepo.crearUsuario(user.tipoCliente, request.contrasena, usuario, request.nombre, request.correo)
          onComplete(resultado) {
            case Success(value) =>
              complete(value.toString)
            case Failure(ex) => execution(ex)
          }
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
