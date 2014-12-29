package co.com.alianza.web.empresa

import co.com.alianza.infrastructure.dto.security.UsuarioAuth
import co.com.alianza.infrastructure.messages.empresa.{CrearAgenteEMessageJsonSupport, CrearAgenteEMessage}
import spray.routing.Directives
import co.com.alianza.app.{CrossHeaders, AlianzaCommons}

/**
 * Created by s4n on 17/12/14.
 */
class UsuarioEmpresaService extends Directives with AlianzaCommons   with CrossHeaders  {

  import CrearAgenteEMessageJsonSupport._

  def route(usuario: UsuarioAuth) = {
    pathPrefix("empresa") {
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
