package co.com.alianza.web


import spray.routing.Directives
import co.com.alianza.app.{CrossHeaders, AlianzaCommons}
import co.com.alianza.infrastructure.messages.{AgregarIPHabitualUsuario, OlvidoContrasenaMessage, UsuariosMessagesJsonSupport, UsuarioMessage}


/**
 *
 * @author seven4n
 */
class UsuarioService  extends Directives with AlianzaCommons   with CrossHeaders{

  import UsuariosMessagesJsonSupport._

  def route = {
     pathPrefix("autoregistro") {
      path("usuario" ) {
        put {
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
     }~ path("olvidoContrasena"){
          //pathEndOrSingleSlash {
            post {
              //Reinicio de contrasena de la cuenta alianza fiduciaria (Implica cambio en el estado del usuario)
              entity(as[OlvidoContrasenaMessage]) {
                olvidarContrasena =>
                  requestExecute(olvidarContrasena, usuariosActor)
            }
          }
        //}
      }
    }
  }

}
