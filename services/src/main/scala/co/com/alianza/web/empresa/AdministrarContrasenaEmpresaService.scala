package co.com.alianza.web.empresa

import co.com.alianza.app.AlianzaCommons
import co.com.alianza.infrastructure.dto.security.UsuarioAuth
import co.com.alianza.infrastructure.messages.empresa.{CambiarContrasenaClienteAdminMessage, ReiniciarContrasenaAgenteEMessage, AdministrarContrasenaEmpresaMessagesJsonSupport}
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
              entity(as[ReiniciarContrasenaAgenteEMessage]) {
                data =>
                  val dataAux: ReiniciarContrasenaAgenteEMessage = data.copy(idClienteAdmin = Some(user.id))
                  requestExecute(dataAux, contrasenasAgenteEmpresarialActor)
              }
            }
          }
        }
      } ~ pathPrefix("actualizarPwClienteAdmin") {
        respondWithMediaType(mediaType) {
          pathEndOrSingleSlash {
            put {
              //Cambiar contrasena por el usuario cliente admin
              entity(as[CambiarContrasenaClienteAdminMessage]) {
                data =>
                  val dataComplete: CambiarContrasenaClienteAdminMessage = data.copy(idUsuario = Some(user.id))
                  requestExecute(dataComplete, contrasenasClienteAdminActor)
              }
            }
          }
        }
      }
    }
  }

}
