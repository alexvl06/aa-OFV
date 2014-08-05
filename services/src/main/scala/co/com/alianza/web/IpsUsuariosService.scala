package co.com.alianza.web

import co.com.alianza.app.AlianzaCommons
import co.com.alianza.infrastructure.dto.security.UsuarioAuth
import co.com.alianza.infrastructure.messages.{ObtenerIpsUsuarioMessage, ActualizarReglasContrasenasMessage, ActualizarReglasContrasenasMessageJsonSupport, InboxMessage}
import spray.routing.Directives

/**
 * Created by david on 16/06/14.
 */
class IpsUsuariosService extends Directives with AlianzaCommons {

  val ipsUsuarios = "ipsUsuarios"

  def route(user: UsuarioAuth) = {

    path(ipsUsuarios) {
      get {
        respondWithMediaType(mediaType) {
          requestExecute(new ObtenerIpsUsuarioMessage(user.id), ipsUsuarioActor)
        }
      } /*~
      post {
        entity(as[ActualizarReglasContrasenasMessage]) {
          listaReglas =>
            respondWithMediaType(mediaType) {
              requestExecute(listaReglas, ipsUsuarioActor)
            }
        }
      }*/
    }
  }

}
