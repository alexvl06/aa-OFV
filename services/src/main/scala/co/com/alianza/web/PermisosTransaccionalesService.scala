package co.com.alianza.web

import spray.routing.Directives

import co.com.alianza.app.{AlianzaActors, MainActors, CrossHeaders, AlianzaCommons}
import co.com.alianza.commons.enumerations.TiposCliente._
import co.com.alianza.infrastructure.messages.{ConsultarPermisosAgenteLoginMessage, PermisosTransaccionalesJsonSupport, GuardarPermisosAgenteMessage, ConsultarPermisosAgenteMessage}
import co.com.alianza.infrastructure.dto.security.UsuarioAuth

/**
 * Created by manuel on 7/01/15.
 */
class PermisosTransaccionalesService extends Directives with AlianzaCommons with CrossHeaders with AlianzaActors {
  import PermisosTransaccionalesJsonSupport._

  val permisoTransaccionalActorSupervisor = MainActors.system.actorSelection(MainActors.permisoTransaccionalActorSupervisor.path)
  val rutaPermisosTx = "permisosTx"
  val permisosLogin = "permisosLogin"

  def route(user: UsuarioAuth) = pathPrefix(rutaPermisosTx) {
    respondWithMediaType(mediaType) {
      post {
        entity(as[GuardarPermisosAgenteMessage]){
          permisosMessage => requestExecute(permisosMessage.copy(idClienteAdmin = if(user.tipoCliente==clienteAdministrador) Some(user.id) else None), permisoTransaccionalActorSupervisor)
        }
      }
    } ~ path(permisosLogin) {
        respondWithMediaType(mediaType) {
          get {
            requestExecute(ConsultarPermisosAgenteLoginMessage(user), permisoTransaccionalActorSupervisor)
          }
        }
    } ~ path(IntNumber) {
      idAgente =>
        respondWithMediaType(mediaType) {
          get {
            requestExecute(ConsultarPermisosAgenteMessage(idAgente), permisoTransaccionalActorSupervisor)
          }
        }
    }
  }

}
