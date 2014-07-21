package co.com.alianza.web


import spray.routing.Directives
import co.com.alianza.app.{CrossHeaders, AlianzaCommons}
import co.com.alianza.infrastructure.messages.{UsuariosMessagesJsonSupport, UsuarioMessage}


/**
 *
 * @author seven4n
 */
class UsuarioService  extends Directives with AlianzaCommons   with CrossHeaders{

  import UsuariosMessagesJsonSupport._

  def route = {
   put {
     pathPrefix("autoregistro") {
      path("usuario" ) {
          entity(as[UsuarioMessage]) {
           usuario =>
            respondWithMediaType(mediaType) {
              clientIP { ip =>
                val nuevoUsuario = usuario.copy(clientIp = Some(ip.value))
                requestExecute(nuevoUsuario, usuariosActor)
              }
            }
          }
      }
     }
    }
  }

}
