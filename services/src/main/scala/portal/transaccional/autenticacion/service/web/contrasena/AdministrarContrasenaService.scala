package portal.transaccional.autenticacion.service.web.contrasena

import co.com.alianza.app.CrossHeaders
import co.com.alianza.commons.enumerations.TiposCliente
import co.com.alianza.exceptions.PersistenceException
import co.com.alianza.infrastructure.auditing.AuditingHelper
import co.com.alianza.infrastructure.auditing.AuditingHelper._
import co.com.alianza.infrastructure.auditing.AuditingUser.AuditingUserData
import co.com.alianza.infrastructure.dto.security.UsuarioAuth
import co.com.alianza.util.token.Token
import portal.transaccional.autenticacion.service.drivers.contrasena.{ ContrasenaAdminRepository, ContrasenaAgenteRepository, ContrasenaUsuarioRepository }
import portal.transaccional.autenticacion.service.util.JsonFormatters.DomainJsonFormatters
import portal.transaccional.autenticacion.service.util.ws.CommonRESTFul

/**
 * Created by seven4n on 01/09/14.
 */
case class AdministrarContrasenaService(kafkaActor: ActorSelection, contrasenaUsuarioRepo: ContrasenaUsuarioRepository,
    contrasenaAgenteRepo: ContrasenaAgenteRepository, contrasenaAdminRepo: ContrasenaAdminRepository)(implicit val ec: ExecutionContext) extends CommonRESTFul with DomainJsonFormatters with CrossHeaders {

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
          entity(as[CambiarContrasenaMessage]) {
            data =>
              mapRequestContext {
                r: RequestContext =>
                  val usuario: Option[AuditingUserData] = getAuditingUser(user.tipoIdentificacion, user.identificacion, user.usuario)
                  requestAuditing[PersistenceException, CambiarContrasenaMessage](r, AuditingHelper.fiduciariaTopic,
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
      entity(as[CambiarContrasenaCaducadaRequestMessage]) {
        data =>
          {
            val claim = (Token.getToken(data.token)).getJWTClaimsSet()
            val us_id = claim.getCustomClaim("us_id").toString.toInt
            val us_tipo = claim.getCustomClaim("us_tipo").toString
            val tipoCliente = TiposCliente.withName(us_tipo)
            tipoCliente match {
              case TiposCliente.agenteEmpresarial =>
                //TODO: refactor en proceso
                /*requestExecute(
                  CambiarContrasenaCaducadaAgenteEmpresarialMessage(data.token, data.pw_actual, data.pw_nuevo, Some(us_id)),
                  contrasenaAgenteRepo
                )*/
                complete("")
              case TiposCliente.clienteAdministrador =>
                /*requestExecute(
                  CambiarContrasenaCaducadaClienteAdminMessage(data.token, data.pw_actual, data.pw_nuevo, Some(us_id)),
                  contrasenaAdminRepo
                )*/
                complete("")
              case TiposCliente.clienteIndividual =>
                //requestExecute(CambiarContrasenaCaducadaMessage(data.token, data.pw_actual, data.pw_nuevo, us_id, us_tipo), contrasenaUsuarioRepo)
                complete("")
            }
          }
      }
    }
  }

}
