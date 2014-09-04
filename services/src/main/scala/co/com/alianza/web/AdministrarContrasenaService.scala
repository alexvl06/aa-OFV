package co.com.alianza.web

import spray.routing.Directives
import co.com.alianza.app.AlianzaCommons
import co.com.alianza.infrastructure.dto.security.UsuarioAuth
import co.com.alianza.infrastructure.messages.{AdministrarContrasenaMessagesJsonSupport, CambiarContrasenaMessage}

/**
 * Created by seven4n on 01/09/14.
 */
class AdministrarContrasenaService extends Directives with AlianzaCommons {

  import AdministrarContrasenaMessagesJsonSupport._

  def route(user: UsuarioAuth) =

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

}
