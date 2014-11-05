package co.com.alianza.web

import co.com.alianza.app.{CrossHeaders, AlianzaCommons}
import co.com.alianza.infrastructure.dto.security.UsuarioAuth
import co.com.alianza.infrastructure.messages._
import co.com.alianza.persistence.entities.IpsUsuario
import spray.routing.Directives

/**
 * Created by david on 16/06/14.
 */
class IpsUsuariosService extends Directives with AlianzaCommons with CrossHeaders {

  import IpsUsuarioMessagesJsonSupport._
  val ipsUsuarios = "ipsUsuarios"
  val ponerIpHabitual = "ponerIpHabitual"

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
              val agregarIpsUsuarioMessageAux: AgregarIpsUsuarioMessage = agregarIpsUsuarioMessage.copy(idUsuario = Some(user.id))
              requestExecute(agregarIpsUsuarioMessageAux, ipsUsuarioActor)
            }
        }
      } ~
      delete {
        entity(as[EliminarIpsUsuarioMessage]) {
          eliminarIpsUsuarioMessage =>
            respondWithMediaType(mediaType) {
              val eliminarIpsUsuarioMessageAux: EliminarIpsUsuarioMessage = eliminarIpsUsuarioMessage.copy(idUsuario = Some(user.id))
              requestExecute(eliminarIpsUsuarioMessageAux, ipsUsuarioActor)
            }
          }
    }
  }

}
