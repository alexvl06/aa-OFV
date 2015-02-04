package co.com.alianza.web

import co.com.alianza.commons.enumerations.TiposCliente
import co.com.alianza.infrastructure.messages.empresa.{CambiarContrasenaAgenteEmpresarialMessage, CambiarContrasenaClienteAdminMessage}
import spray.routing.Directives
import co.com.alianza.app.AlianzaCommons
import co.com.alianza.infrastructure.dto.security.UsuarioAuth
import co.com.alianza.infrastructure.messages._
import co.com.alianza.util.token.Token

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
            //Cambiar contrasena de la cuenta alianza valores
            entity(as[CambiarContrasenaMessage]) {
              data =>
                val dataComplete: CambiarContrasenaMessage = data.copy(idUsuario = Some(user.id))
                requestExecute(dataComplete, contrasenasActor)
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
                    requestExecute(CambiarContrasenaAgenteEmpresarialMessage(data.pw_actual, data.pw_nuevo, Some(us_id)), contrasenasAgenteEmpresarialActor)
                  case TiposCliente.clienteAdministrador =>
                    requestExecute(CambiarContrasenaClienteAdminMessage(data.pw_actual, data.pw_nuevo, Some(us_id)), contrasenasClienteAdminActor)
                  case TiposCliente.clienteIndividual =>
                    requestExecute(CambiarContrasenaCaducadaMessage(data.token, data.pw_actual, data.pw_nuevo, us_id , us_tipo ), contrasenasActor)
                  case _ => {
                    request => println("en la inmundA!!")
                  }
                }

              }
            }
          }
        }
      }
    }
  }

}
