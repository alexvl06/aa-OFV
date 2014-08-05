package co.com.alianza.web

import co.com.alianza.app.AlianzaCommons
import co.com.alianza.infrastructure.dto.security.UsuarioAuth
import co.com.alianza.infrastructure.messages._
import co.com.alianza.persistence.entities.IpsUsuario
import spray.routing.Directives

/**
 * Created by david on 16/06/14.
 */
class IpsUsuariosService extends Directives with AlianzaCommons {

  import IpsUsuarioMessagesJsonSupport._
  val ipsUsuarios = "ipsUsuarios"

  def route(user: UsuarioAuth) = {

    path(ipsUsuarios) {
      get {
        respondWithMediaType(mediaType) {
          requestExecute(new ObtenerIpsUsuarioMessage(user.id), ipsUsuarioActor)
        }
      } ~
      put {
        entity(as[AgregarIpsUsuarioMessage]) {
          agregarIpsUsuarioMessage =>
            respondWithMediaType(mediaType) {
              requestExecute(agregarIpsUsuarioMessage, ipsUsuarioActor)
            }
        }
      }
    }
  }

}
