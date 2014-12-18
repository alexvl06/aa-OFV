package co.com.alianza.web.empresa

import spray.routing.Directives
import co.com.alianza.app.{AlianzaActors, MainActors, CrossHeaders, AlianzaCommons}
import co.com.alianza.infrastructure.messages.empresa._
import akka.actor.ActorSystem

/**
 * Created by s4n on 17/12/14.
 */
class UsuarioEmpresaService extends Directives with AlianzaCommons   with CrossHeaders with AlianzaActors {

  import UsuariosEmpresaMessagesJsonSupport._

  val system: ActorSystem = MainActors.system

  def route = {
    pathPrefix("empresa") {
      path("consultarUsuarios") {
        respondWithMediaType(mediaType) {
          get {
            parameters('correo.?, 'identificacion.?, 'tipoIdentificacion.?, 'estadoUsuario.?) { (correo, identificacion, tipoIdentificacion, estadoUsuario) =>
              //Lista de todos los usuarios
              requestExecute(GetUsuariosEmpresaBusquedaMessage(correo.getOrElse(null), identificacion.getOrElse(null), tipoIdentificacion.getOrElse(null), estadoUsuario.getOrElse(null)), usuariosActor)
            }
          }
        }
      }
    }
  }
}
