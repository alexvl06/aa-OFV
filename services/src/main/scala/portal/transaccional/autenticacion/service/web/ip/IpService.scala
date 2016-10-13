package portal.transaccional.autenticacion.service.web.ip

import akka.actor.ActorSelection
import co.com.alianza.app.CrossHeaders
import co.com.alianza.exceptions.PersistenceException
import co.com.alianza.infrastructure.auditing.AuditingHelper
import co.com.alianza.infrastructure.auditing.AuditingHelper.getAuditingUser
import co.com.alianza.infrastructure.auditing.AuditingHelper.requestAuditing
import co.com.alianza.infrastructure.auditing.AuditingUser.AuditingUserData
import co.com.alianza.infrastructure.dto.security.UsuarioAuth
import portal.transaccional.autenticacion.service.drivers.ip.IpRepository
import portal.transaccional.autenticacion.service.util.JsonFormatters.DomainJsonFormatters
import portal.transaccional.autenticacion.service.util.ws.CommonRESTFul
import spray.http.StatusCodes
import spray.routing._

import scala.concurrent.{ Future, ExecutionContext }
import scala.util.{ Failure, Success }

/**
 * Created by s4n on 2016
 */
case class IpService(user: UsuarioAuth, kafkaActor: ActorSelection, ipRepo: IpRepository)(implicit val ec: ExecutionContext)
    extends CommonRESTFul with DomainJsonFormatters with CrossHeaders {

  val ipsUsuarios = "ipsUsuarios"
  val ponerIpHabitual = "ponerIpHabitual"

  val route: Route = {
    path(ipsUsuarios) {
      pathEndOrSingleSlash {
        clientIP { ip =>
          obtener(ip.value) ~ eliminar(ip.value) ~ agregar(ip.value)
        }
      }
    } ~ path(ponerIpHabitual) {
      pathEndOrSingleSlash {
        clientIP { ip =>
          agregarHabitual(ip.value)
        }
      }
    }
  }

  private def obtener(ipPeticion: String) = {
    get {
      mapRequestContext {
        r: RequestContext =>
          requestAuditing[PersistenceException, Any](r, AuditingHelper.fiduciariaTopic, AuditingHelper.usuarioConsultarIpIndex,
            ipPeticion, kafkaActor, getAuditingUser(user.tipoIdentificacion, user.identificacion, user.usuario), None)
      } {
        val resultado: Future[Seq[IpResponse]] = ipRepo.obtenerIps(user)
        onComplete(resultado) {
          case Success(value) => complete(value)
          case Failure(ex) => complete((StatusCodes.Conflict, "Error obtener"))
        }
      }
    }
  }

  private def eliminar(ipPeticion: String) = {
    delete {
      entity(as[IpRequest]) {
        eliminarIp =>
          mapRequestContext {
            r: RequestContext =>
              val usuario: Option[AuditingUserData] = getAuditingUser(user.tipoIdentificacion, user.identificacion, user.usuario)
              requestAuditing[PersistenceException, IpRequest](r, AuditingHelper.fiduciariaTopic,
                AuditingHelper.usuarioEliminarIpIndex, ipPeticion, kafkaActor, usuario, Some(eliminarIp))
          } {
            val resultado: Future[Int] = ipRepo.eliminarIp(user, eliminarIp.ip)
            onComplete(resultado) {
              case Success(value) => complete(value.toString)
              case Failure(ex) => complete((StatusCodes.Conflict, "Error al eliminar"))
            }
          }
      }
    }
  }

  private def agregar(ipPeticion: String) = {
    put {
      entity(as[IpRequest]) {
        agregarIp =>
          mapRequestContext {
            r: RequestContext =>
              val usuario: Option[AuditingUserData] = getAuditingUser(user.tipoIdentificacion, user.identificacion, user.usuario)
              requestAuditing[PersistenceException, IpRequest](r, AuditingHelper.fiduciariaTopic,
                AuditingHelper.usuarioAgregarIpIndex, ipPeticion, kafkaActor, usuario, Some(agregarIp))
          } {
            val resultado: Future[String] = ipRepo.agregarIp(user, agregarIp.ip)
            onComplete(resultado) {
              case Success(value) => complete("Registro de IP Exitoso")
              case Failure(ex) => complete((StatusCodes.Conflict, "Error al agregar"))
            }
          }
      }
    }
  }

  private def agregarHabitual(ipPeticion: String) = {
    get {
      mapRequestContext {
        r: RequestContext =>
          val usuario: Option[AuditingUserData] = getAuditingUser(user.tipoIdentificacion, user.identificacion, user.usuario)
          requestAuditing[PersistenceException, String](r, AuditingHelper.fiduciariaTopic, AuditingHelper.usuarioConsultarIpIndex,
            ipPeticion, kafkaActor, usuario, Some(ipPeticion))
      } {
        val resultado: Future[String] = ipRepo.agregarIp(user, ipPeticion)
        onComplete(resultado) {
          case Success(value) => complete(value)
          case Failure(ex) => complete((StatusCodes.Conflict, "Error al agregar"))
        }
      }
    }
  }

}