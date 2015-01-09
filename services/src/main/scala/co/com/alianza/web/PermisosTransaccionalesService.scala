package co.com.alianza.web

import spray.routing.Directives
import co.com.alianza.app.{AlianzaActors, MainActors, CrossHeaders, AlianzaCommons}
import co.com.alianza.infrastructure.messages.{PermisosTransaccionalesJsonSupport, GuardarPermisosAgenteMessage}
import co.com.alianza.infrastructure.dto.security.UsuarioAuth

/**
 * Created by manuel on 7/01/15.
 */
class PermisosTransaccionalesService extends Directives with AlianzaCommons with CrossHeaders with AlianzaActors {
  import PermisosTransaccionalesJsonSupport._

  def route(user: UsuarioAuth) = path("permisosTx") {
    respondWithMediaType(mediaType) {
      post {
        entity(as[GuardarPermisosAgenteMessage]){
          permisosMessage => requestExecute(permisosMessage, MainActors.system.actorSelection(MainActors.permisoTransaccionalActorSupervisor.path))
        }
      }
    }
  }

}
