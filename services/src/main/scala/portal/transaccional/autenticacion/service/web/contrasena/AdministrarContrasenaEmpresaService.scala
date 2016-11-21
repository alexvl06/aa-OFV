package portal.transaccional.autenticacion.service.web.contrasena

import akka.actor.ActorSelection
import co.com.alianza.app.CrossHeaders
import co.com.alianza.exceptions.{ ValidacionException, PersistenceException }
import co.com.alianza.infrastructure.auditing.AuditingHelper
import co.com.alianza.infrastructure.auditing.AuditingHelper._
import co.com.alianza.infrastructure.auditing.AuditingUser.AuditingUserData
import co.com.alianza.infrastructure.dto.security.UsuarioAuth
import portal.transaccional.autenticacion.service.drivers.contrasena.{ ContrasenaAdminRepository, ContrasenaAgenteRepository }
import portal.transaccional.autenticacion.service.util.JsonFormatters.DomainJsonFormatters
import portal.transaccional.autenticacion.service.util.ws.CommonRESTFul
import spray.http.StatusCodes
import spray.http.StatusCodes._
import spray.routing.{ StandardRoute, RequestContext }

import scala.concurrent.{ Future, ExecutionContext }
import scala.util.Failure

/**
 * Created by S4N on 17/12/14.
 */
case class AdministrarContrasenaEmpresaService(user: UsuarioAuth, kafkaActor: ActorSelection, contrasenaAgenteRepo: ContrasenaAgenteRepository,
    contrasenaAdminRepo: ContrasenaAdminRepository)(implicit val ec: ExecutionContext) extends CommonRESTFul with DomainJsonFormatters with CrossHeaders {

  def secureRouteEmpresa = {
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
      entity(as[CambiarContrasenaAgenteEmpresarialMessage]) {
        data =>
          clientIP {
            ip =>
              mapRequestContext {
                r: RequestContext =>
                  val usuario: Option[AuditingUserData] = getAuditingUser(user.tipoIdentificacion, user.identificacion, user.usuario)
                  requestAuditing[PersistenceException, CambiarContrasenaAgenteEmpresarialMessage](r, AuditingHelper.fiduciariaTopic,
                    AuditingHelper.cambioContrasenaAgenteEmpresarialIndex, ip.value, kafkaActor, usuario, Some(data.copy(pw_nuevo = null, pw_actual = null)))
              } {
                val resultado: Future[Boolean] = contrasenaAgenteRepo.cambiarContrasena(user, data.pw_nuevo, data.pw_actual)
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
      entity(as[CambiarContrasenaClienteAdminMessage]) {
        data =>
          clientIP {
            ip =>
              //TODO: refactor en proceso
              mapRequestContext {
                r: RequestContext =>
                  val msg: CambiarContrasenaClienteAdminMessage = data.copy(idUsuario = Some(user.id))
                  val usuario: Option[AuditingUserData] = getAuditingUser(user.tipoIdentificacion, user.identificacion, user.usuario)
                  requestAuditing[PersistenceException, CambiarContrasenaClienteAdminMessage](r, AuditingHelper.fiduciariaTopic,
                    AuditingHelper.cambioContrasenaClienteAdministradorIndex, ip.value, kafkaActor, usuario, Some(msg.copy(pw_nuevo = null, pw_actual = null)))
              } {
                /*val resultado: Future[Int] = horarioEmpresaRepository.agregar(user, request.diaHabil, request.sabado, request.horaInicio, request.horaFin)
                onComplete(resultado) {
                  case Success(value) => complete(value.toString)
                  case Failure(ex) => execution(ex)
                }
                val dataComplete: CambiarContrasenaClienteAdminMessage = data.copy(idUsuario = Some(user.id))
                requestExecute(dataComplete, contrasenaAdminRepo)
                */
                complete("")
              }
          }
      }
    }
  }

  private def execution(ex: Throwable): StandardRoute = {
    ex match {
      case ex: ValidacionException => complete((StatusCodes.Conflict, ex))
      case ex: PersistenceException =>
        ex.printStackTrace(); complete((StatusCodes.InternalServerError, "Error inesperado"))
      case ex: Throwable => ex.printStackTrace(); complete((StatusCodes.InternalServerError, "Error inesperado"))
    }
  }

}
