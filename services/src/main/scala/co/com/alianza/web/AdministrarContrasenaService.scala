package co.com.alianza.web

import co.com.alianza.commons.enumerations.TiposCliente
import co.com.alianza.infrastructure.auditing.AuditingHelper
import co.com.alianza.infrastructure.messages.empresa.{CambiarContrasenaCaducadaClienteAdminMessage, CambiarContrasenaCaducadaAgenteEmpresarialMessage}
import co.com.alianza.infrastructure.messages._
import co.com.alianza.util.token.Token
import co.com.alianza.exceptions.PersistenceException
import co.com.alianza.infrastructure.anticorruption.usuarios.DataAccessAdapter
import co.com.alianza.infrastructure.auditing.AuditingHelper._
import co.com.alianza.util.clave.Crypto
import enumerations.AppendPasswordUser
import spray.routing.{RequestContext, Directives}
import co.com.alianza.app.AlianzaCommons
import co.com.alianza.infrastructure.dto.security.UsuarioAuth
import co.com.alianza.infrastructure.messages.{CambiarContrasenaCaducadaMessage, AdministrarContrasenaMessagesJsonSupport, CambiarContrasenaMessage}

/**
 * Created by seven4n on 01/09/14.
 */
class AdministrarContrasenaService extends Directives with AlianzaCommons {

  import AdministrarContrasenaMessagesJsonSupport._

  def secureRoute(user: UsuarioAuth) =
    pathPrefix("actualizarContrasena") {
      respondWithMediaType(mediaType) {
        pathEndOrSingleSlash {
          put {
            clientIP {
              ip =>
                //Cambiar contrasena de la cuenta alianza valores
                entity(as[CambiarContrasenaMessage]) {
                  data =>
                    mapRequestContext {
                      r: RequestContext =>
                        val token = r.request.headers.find(header => header.name equals "token")
                        val usuario = DataAccessAdapter.obtenerTipoIdentificacionYNumeroIdentificacionUsuarioToken(token.get.value)

                        requestWithFutureAuditing[PersistenceException, CambiarContrasenaMessage](r, AuditingHelper.fiduciariaTopic, AuditingHelper.cambioContrasenaIndex, ip.value, kafkaActor, usuario, Some(data.copy(pw_actual = Crypto.hashSha512(data.pw_actual.concat(AppendPasswordUser.appendUsuariosFiducia)), pw_nuevo = Crypto.hashSha512(data.pw_nuevo.concat(AppendPasswordUser.appendUsuariosFiducia)))))
                    } {
                      val dataComplete: CambiarContrasenaMessage = data.copy(idUsuario = Some(user.id))
                      requestExecute(dataComplete, contrasenasActor)
                    }
                }
            }
          }
        }
      }
    }

      def insecureRoute = {
        pathPrefix("actualizarContrasenaCaducada") {
          respondWithMediaType(mediaType) {
            pathEndOrSingleSlash {
              put {
                entity(as[CambiarContrasenaCaducadaRequestMessage]) {
                  data => {

                    val claim = (Token.getToken(data.token)).getJWTClaimsSet()
                    val us_id = claim.getCustomClaim("us_id").toString.toInt
                    val us_tipo = claim.getCustomClaim("us_tipo").toString
                    val tipoCliente = TiposCliente.withName(us_tipo)

                    tipoCliente match {
                      case TiposCliente.agenteEmpresarial =>
                        requestExecute(CambiarContrasenaCaducadaAgenteEmpresarialMessage(data.token, data.pw_actual, data.pw_nuevo, Some(us_id)), contrasenasAgenteEmpresarialActor)
                      case TiposCliente.clienteAdministrador =>
                        requestExecute(CambiarContrasenaCaducadaClienteAdminMessage(data.token, data.pw_actual, data.pw_nuevo, Some(us_id)), contrasenasClienteAdminActor)
                      case TiposCliente.clienteIndividual =>
                        requestExecute(CambiarContrasenaCaducadaMessage(data.token, data.pw_actual, data.pw_nuevo, us_id, us_tipo), contrasenasActor)
                    }
                  }
                }
              }
            }
          }
        }
      }


}
