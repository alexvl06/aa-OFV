package portal.transaccional.autenticacion.service.web.recursoComercial

import akka.actor.ActorSelection
import co.com.alianza.app.CrossHeaders
import co.com.alianza.exceptions._
import co.com.alianza.infrastructure.auditing.AuditingHelper
import co.com.alianza.infrastructure.auditing.AuditingHelper._
import co.com.alianza.infrastructure.auditing.AuditingUser.AuditingUserData
import co.com.alianza.infrastructure.dto.security.UsuarioAuth
import portal.transaccional.autenticacion.service.drivers.rolRecursoComercial.{ RecursoComercialRepository, RolComercialRepository }
import portal.transaccional.autenticacion.service.util.JsonFormatters.DomainJsonFormatters
import portal.transaccional.autenticacion.service.util.ws.CommonRESTFul
import portal.transaccional.autenticacion.service.web.actualizacion.ActualizacionMessage
import spray.http.StatusCodes
import spray.routing.{ RequestContext, Route, StandardRoute }

import scala.concurrent.ExecutionContext
import scala.util.{ Failure, Success }

/**
 * Created by dfbaratov on 23/08/16.
 */

case class RecursoGraficoComercialService(user: UsuarioAuth, kafkaActor: ActorSelection, recursoComercialRepository: RecursoComercialRepository,
    rolComercialRepository: RolComercialRepository)(implicit val ec: ExecutionContext) extends CommonRESTFul with DomainJsonFormatters with CrossHeaders {

  val recursoComercialPath = "recursoComercial"
  val adminPath = "admin"
  val rolesPath = "roles"
  val recursosPath = "recursos"

  val route: Route = {
    pathPrefix(recursoComercialPath / adminPath) {
      path(rolesPath) {
        roles()
      } ~
        path(recursosPath) {
          recursos()
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
      clientIP {
        ip =>
          mapRequestContext {
            r: RequestContext =>
              val usuario: Option[AuditingUserData] = getAuditingUser(user.tipoIdentificacion, user.identificacion, user.usuario)
              requestAuditing[PersistenceException, ActualizacionMessage](r, AuditingHelper.fiduciariaTopic, AuditingHelper.recursosComercialIndex,
                ip.value, kafkaActor, usuario, None)
          } {
            val recursos = recursoComercialRepository.obtenerTodosConRoles()
            onComplete(recursos) {
              case Success(value) => complete(value)
              case Failure(ex) => execution(ex)
            }
          }
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