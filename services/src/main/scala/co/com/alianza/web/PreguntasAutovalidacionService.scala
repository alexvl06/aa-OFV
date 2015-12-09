package co.com.alianza.web

import co.com.alianza.app.{AlianzaCommons, CrossHeaders}
import co.com.alianza.infrastructure.dto.security.UsuarioAuth
import co.com.alianza.infrastructure.messages.{GuardarRespuestasMessage, ObtenerPreguntasMessage}
import spray.routing.{Directives}

/**
 *
 * @author seven4n
 */
class PreguntasAutovalidacionService  extends Directives with AlianzaCommons   with CrossHeaders{

  import co.com.alianza.infrastructure.messages.PreguntasAutovalidacionMessagesJsonSupport._

  def route(user: UsuarioAuth) = {
    pathPrefix("preguntasAutovalidacion") {
      get {
        respondWithMediaType(mediaType) {
          requestExecute(new ObtenerPreguntasMessage, preguntasAutovalidacionActor)
        }
      } ~ put {
        entity(as[GuardarRespuestasMessage]) {
          message =>
            respondWithMediaType(mediaType) {
              requestExecute(message.copy(idUsuario = Some(user.id), tipoCliente = Some(user.tipoCliente.toString)), preguntasAutovalidacionActor)
            }
        }
      }
    }
  }
}
