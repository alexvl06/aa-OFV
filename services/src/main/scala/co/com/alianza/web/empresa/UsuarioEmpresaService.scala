package co.com.alianza.web.empresa

import co.com.alianza.infrastructure.dto.security.UsuarioAuth
import co.com.alianza.infrastructure.messages.empresa.{CrearAgenteEMessageJsonSupport, CrearAgenteEMessage}
import spray.routing.Directives
import co.com.alianza.app.{AlianzaActors, MainActors, CrossHeaders, AlianzaCommons}
import co.com.alianza.infrastructure.messages.empresa._
import akka.actor.ActorSystem
import co.com.alianza.infrastructure.dto.security.UsuarioAuth

/**
 * Created by s4n on 17/12/14.
 */
class UsuarioEmpresaService extends Directives with AlianzaCommons   with CrossHeaders with AlianzaActors {

  import CrearAgenteEMessageJsonSupport._

  def secureUserRouteEmpresa(user: UsuarioAuth) = {
    pathPrefix("empresa") {
      path("consultarUsuarios") {
        respondWithMediaType(mediaType) {
          get {
            parameters('correo.?, 'usuario.?, 'nombre.?, 'estado.?) { (correo, usuario, nombre, estado) =>
              //Lista de todos los usuarios
              requestExecute(GetUsuariosEmpresaBusquedaMessage(correo.getOrElse(null), usuario.getOrElse(null), nombre.getOrElse(null), estado.get.toInt, user.id), usuariosEmpresarialActor)
            }
          }
        }
      } ~
        path("usuarioAgenteEmpresarial") {
          respondWithMediaType(mediaType) {
            pathEndOrSingleSlash {
              put {
                entity(as[CrearAgenteEMessage]) {
                  data =>
                    requestExecute(data, agenteEmpresarialActor)
                }
              }
            }
          }
        }
    }

  }
}
