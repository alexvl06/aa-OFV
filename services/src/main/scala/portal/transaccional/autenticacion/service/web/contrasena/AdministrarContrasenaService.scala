package portal.transaccional.autenticacion.service.web.contrasena

import akka.actor.ActorSelection
import co.com.alianza.app.CrossHeaders
import co.com.alianza.commons.enumerations.TiposCliente
import co.com.alianza.exceptions.{ PersistenceException, ValidacionException, ValidacionExceptionPasswordRules }
import co.com.alianza.infrastructure.auditing.AuditingHelper
import co.com.alianza.infrastructure.auditing.AuditingHelper._
import co.com.alianza.infrastructure.auditing.AuditingUser.AuditingUserData
import co.com.alianza.infrastructure.dto.security.UsuarioAuth
import co.com.alianza.util.token.Token
import portal.transaccional.autenticacion.service.drivers.contrasena.{ ContrasenaAdminRepository, ContrasenaAgenteRepository, ContrasenaUsuarioRepository }
import portal.transaccional.autenticacion.service.drivers.contrasenaAgenteInmobiliario.ContrasenaAgenteInmobiliarioRepository
import portal.transaccional.autenticacion.service.util.JsonFormatters.DomainJsonFormatters
import portal.transaccional.autenticacion.service.util.ws.CommonRESTFul
import spray.http.StatusCodes._
import spray.routing.{ RequestContext, StandardRoute }

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Success }

/**
 * Created by seven4n on 01/09/14.
 */
case class AdministrarContrasenaService(
    kafkaActor: ActorSelection,
    contrasenaUsuarioRepo: ContrasenaUsuarioRepository,
    contrasenaAgenteRepo: ContrasenaAgenteRepository,
    contrasenaAdminRepo: ContrasenaAdminRepository,
    agenteInmobContrasenaRepo: ContrasenaAgenteInmobiliarioRepository
)(implicit val ec: ExecutionContext) extends CommonRESTFul with DomainJsonFormatters with CrossHeaders {

  def routeSeguro(user: UsuarioAuth) = {
    pathPrefix("actualizarContrasena") {
      pathEndOrSingleSlash {
        actualizarContrasena(user)
      }
    }
  }

  private def actualizarContrasena(user: UsuarioAuth) = {
    put {
      clientIP {
        ip =>
          entity(as[CambiarContrasena]) {
            data =>
              mapRequestContext {
                r: RequestContext =>
                  val usuario: Option[AuditingUserData] = getAuditingUser(user.tipoIdentificacion, user.identificacion, user.usuario)
                  requestAuditing[PersistenceException, CambiarContrasena](r, AuditingHelper.fiduciariaTopic,
                    AuditingHelper.cambioContrasenaIndex, ip.value, kafkaActor, usuario, Some(data.copy(pw_actual = null, pw_nuevo = null)))
              } {
                val resultado: Future[Int] = contrasenaUsuarioRepo.cambiarContrasena(user.id, data.pw_nuevo, data.pw_actual)
                onComplete(resultado) {
                  case Success(value) => complete(value.toString)
                  case Failure(ex) => execution(ex)
                }
              }
          }
      }
    }
  }

  def route = {
    pathPrefix("actualizarContrasenaCaducada") {
      pathEndOrSingleSlash {
        cambiarContrasenaCaducada
      }
    }
  }

  private def cambiarContrasenaCaducada = {
    put {
      entity(as[CambiarContrasenaCaducada]) {
        data =>
          {
            val claim = (Token.getToken(data.token)).getJWTClaimsSet()
            val us_id = claim.getCustomClaim("us_id").toString.toInt
            val us_tipo = claim.getCustomClaim("us_tipo").toString
            val tipoCliente = TiposCliente.withName(us_tipo)

            val resultado = tipoCliente match {
              case TiposCliente.agenteEmpresarial => contrasenaAgenteRepo.cambiarContrasena(us_id, data.pw_nuevo, data.pw_actual)
              case TiposCliente.clienteAdministrador | TiposCliente.clienteAdminInmobiliario => contrasenaAdminRepo.cambiarContrasena(us_id, data.pw_nuevo, data.pw_actual)
              case TiposCliente.clienteIndividual => contrasenaUsuarioRepo.cambiarContrasena(us_id, data.pw_nuevo, data.pw_actual)
              case TiposCliente.agenteInmobiliario | TiposCliente.agenteInmobiliarioInterno => agenteInmobContrasenaRepo.actualizarContrasenaCaducada(Option(data.token), data.pw_actual, data.pw_nuevo, Option.empty)
            }

            onComplete(resultado) {
              case Success(value) => complete(value.toString)
              case Failure(ex) => execution(ex)
            }
          }
      }
    }
  }

  private def execution(ex: Throwable): StandardRoute = {
    ex match {
      case ex: ValidacionException => complete((Conflict, ex))
      case ex: ValidacionExceptionPasswordRules => complete((Conflict, ex.copy(data = ex.detail, code = "409.12")))
      case ex: PersistenceException =>
        ex.printStackTrace(); complete((InternalServerError, "Error inesperado"))
      case ex: Throwable => ex.printStackTrace(); complete((InternalServerError, "Error inesperado"))
    }
  }

}

