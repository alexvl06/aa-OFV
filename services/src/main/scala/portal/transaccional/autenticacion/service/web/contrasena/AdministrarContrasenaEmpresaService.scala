package portal.transaccional.autenticacion.service.web.contrasena

import akka.actor.ActorSelection
import co.com.alianza.app.CrossHeaders
import co.com.alianza.exceptions.{ PersistenceException, ValidacionException }
import co.com.alianza.infrastructure.auditing.AuditingHelper
import co.com.alianza.infrastructure.auditing.AuditingHelper._
import co.com.alianza.infrastructure.auditing.AuditingUser.AuditingUserData
import co.com.alianza.infrastructure.dto.security.UsuarioAuth
import portal.transaccional.autenticacion.service.drivers.contrasena.{ ContrasenaAdminRepository, ContrasenaAgenteRepository }
import portal.transaccional.autenticacion.service.util.JsonFormatters.DomainJsonFormatters
import portal.transaccional.autenticacion.service.util.ws.CommonRESTFul
import spray.http.StatusCodes._
import spray.routing.{ RequestContext, StandardRoute }

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Success }

/**
 * Created by S4N on 17/12/14.
 */
case class AdministrarContrasenaEmpresaService(user: UsuarioAuth, kafkaActor: ActorSelection, contrasenaAgenteRepo: ContrasenaAgenteRepository,
    contrasenaAdminRepo: ContrasenaAdminRepository)(implicit val ec: ExecutionContext) extends CommonRESTFul with DomainJsonFormatters with CrossHeaders {

  def route = {
    pathPrefix("empresa") {
      path("reiniciarContrasena") {
        pathEndOrSingleSlash {
          reiniciarContrasenaAgente
        }
      } ~ pathPrefix("bloquearDesbloquearAgente") {
        pathEndOrSingleSlash {
          cambiarEstadoAgente
        }
      } ~ pathPrefix("actualizarPwAgenteEmpresarial") {
        pathEndOrSingleSlash {
          actualizarContrasenaAgente
        }
      } ~ pathPrefix("actualizarPwClienteAdmin") {
        pathEndOrSingleSlash {
          actualizarContrasenaAdmin
        }
      }
    }
  }

  private def reiniciarContrasenaAgente = {
    put {
      entity(as[ReiniciarContrasenaAgente]) {
        data =>
          clientIP {
            ip =>
              mapRequestContext {
                r: RequestContext =>
                  val usuario: Option[AuditingUserData] = getAuditingUser(user.tipoIdentificacion, user.identificacion, user.usuario)
                  requestAuditing[PersistenceException, ReiniciarContrasenaAgente](r, AuditingHelper.fiduciariaTopic,
                    AuditingHelper.reiniciarContrasenaAgenteEmpresarialIndex, ip.value, kafkaActor, usuario, Some(data))
              } {
                val resultado: Future[Boolean] = contrasenaAgenteRepo.reiniciarContrasena(user, data.usuario)
                onComplete(resultado) {
                  case Success(value) => complete(Created)
                  case Failure(ex) => execution(ex)
                }
              }
          }
      }
    }
  }

  private def cambiarEstadoAgente = {
    put {
      entity(as[CambiarEstadoAgente]) {
        data =>
          clientIP {
            ip =>
              mapRequestContext {
                r: RequestContext =>
                  val usuario: Option[AuditingUserData] = getAuditingUser(user.tipoIdentificacion, user.identificacion, user.usuario)
                  requestAuditing[PersistenceException, CambiarEstadoAgente](r, AuditingHelper.fiduciariaTopic,
                    AuditingHelper.bloqueoAgenteEmpresarialIndex, ip.value, kafkaActor, usuario, Some(data))
              } {
                val resultado: Future[Boolean] = contrasenaAgenteRepo.cambiarEstado(user, data.usuario)
                onComplete(resultado) {
                  case Success(value) => complete(OK)
                  case Failure(ex) => execution(ex)
                }
              }
          }
      }
    }
  }

  private def actualizarContrasenaAgente = {
    put {
      entity(as[CambiarContrasena]) {
        data =>
          clientIP {
            ip =>
              mapRequestContext {
                r: RequestContext =>
                  val usuario: Option[AuditingUserData] = getAuditingUser(user.tipoIdentificacion, user.identificacion, user.usuario)
                  requestAuditing[PersistenceException, CambiarContrasena](r, AuditingHelper.fiduciariaTopic,
                    AuditingHelper.cambioContrasenaAgenteEmpresarialIndex, ip.value, kafkaActor, usuario, Some(data.copy(pw_nuevo = null, pw_actual = null)))
              } {
                val resultado: Future[Int] = contrasenaAgenteRepo.cambiarContrasena(user.id, data.pw_nuevo, data.pw_actual)
                onComplete(resultado) {
                  case Success(value) => complete(OK)
                  case Failure(ex) => execution(ex)
                }
              }
          }
      }
    }
  }

  private def actualizarContrasenaAdmin = {
    put {
      entity(as[CambiarContrasena]) {
        data =>
          clientIP {
            ip =>
              mapRequestContext {
                r: RequestContext =>
                  val usuario: Option[AuditingUserData] = getAuditingUser(user.tipoIdentificacion, user.identificacion, user.usuario)
                  requestAuditing[PersistenceException, CambiarContrasena](r, AuditingHelper.fiduciariaTopic,
                    cambioContrasenaClienteAdministradorIndex, ip.value, kafkaActor, usuario, Some(data.copy(pw_nuevo = null, pw_actual = null)))
              } {
                val resultado: Future[Int] = contrasenaAdminRepo.cambiarContrasena(user.id, data.pw_nuevo, data.pw_actual)
                onComplete(resultado) {
                  case Success(value) => complete(OK)
                  case Failure(ex) => execution(ex)
                }
              }
          }
      }
    }
  }

  private def execution(ex: Throwable): StandardRoute = {
    ex match {
      case ex: ValidacionException => complete((Conflict, ex))
      case ex: PersistenceException =>
        ex.printStackTrace(); complete((InternalServerError, "Error inesperado"))
      case ex: Throwable => ex.printStackTrace(); complete((InternalServerError, "Error inesperado"))
    }
  }

}
