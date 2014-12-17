package co.com.alianza.web.empresa

import co.com.alianza.app.AlianzaCommons
import co.com.alianza.infrastructure.dto.security.UsuarioAuth
import co.com.alianza.infrastructure.messages.empresa.ReiniciarContrasenaEmpresaMessage
import co.com.alianza.infrastructure.messages.empresa.AdministrarContrasenaEmpresaMessagesJsonSupport
import spray.routing.Directives

/**
 * Created by S4N on 17/12/14.
 */
class AdministrarContrasenaEmpresaService extends Directives with AlianzaCommons {

  import AdministrarContrasenaEmpresaMessagesJsonSupport._

  def secureRouteEmpresa(user: UsuarioAuth) = {
    pathPrefix("empresa") {
      path("reiniciarContrasena") {
        respondWithMediaType(mediaType) {
          pathEndOrSingleSlash {
            put {
              //Cambiar contrasena de la cuenta alianza valores
              entity(as[ReiniciarContrasenaEmpresaMessage]) {
                data =>
                  println("-------------------------------------")
                  println(user)
                  println("-------------------------------------")
                  requestExecute(data, contrasenasEmpresaActor)
              }
            }
          }
        }
      }
    }
  }

}
