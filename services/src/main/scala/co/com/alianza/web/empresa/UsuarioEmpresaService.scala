package co.com.alianza.web.empresa

import spray.routing.Directives
import co.com.alianza.app.{AlianzaActors, MainActors, CrossHeaders, AlianzaCommons}
import co.com.alianza.infrastructure.messages.empresa._
import akka.actor.ActorSystem
import co.com.alianza.infrastructure.dto.security.UsuarioAuth

/**
 * Created by s4n on 17/12/14.
 */
class UsuarioEmpresaService extends Directives with AlianzaCommons   with CrossHeaders with AlianzaActors {

  import UsuariosEmpresaMessagesJsonSupport._

  def secureUserRouteEmpresa(user: UsuarioAuth) = {
    pathPrefix("empresa") {
      path("consultarUsuarios") {
        respondWithMediaType(mediaType) {
          get {
            parameters('correo.?, 'usuario.?, 'nombre.?, 'estado.?) { (correo, usuario, nombre, estado) =>
              //Lista de todos los usuarios
              requestExecute(GetUsuariosEmpresaBusquedaMessage(correo.getOrElse(null), usuario.getOrElse(null), nombre.getOrElse(null), estado.getOrElse(null), user.id), usuariosEmpresarialActor)
            }
          }
        }
      }
    }
  }
}
