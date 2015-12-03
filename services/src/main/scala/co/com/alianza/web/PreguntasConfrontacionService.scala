package co.com.alianza.web

import co.com.alianza.app.{AlianzaCommons, CrossHeaders}
import co.com.alianza.infrastructure.dto.security.UsuarioAuth
import co.com.alianza.infrastructure.messages.{GuardarRespuestasMessage, ObtenerPreguntasMessage}
import spray.routing.{Directives}


/**
 *
 * @author seven4n
 */
class PreguntasConfrontacionService  extends Directives with AlianzaCommons   with CrossHeaders{

  import co.com.alianza.infrastructure.messages.PreguntasConfrontacionMessagesJsonSupport._

  def route(user: UsuarioAuth) = {
    pathPrefix("preguntas") {
      get {
        respondWithMediaType(mediaType) {
          pathPrefix("obtenerPreguntas"){
            requestExecute(new ObtenerPreguntasMessage, preguntasConfrontacionActor)
          }
        }
      }~ path("guardarRespuestas") {
        put {
          entity(as[GuardarRespuestasMessage]) {
            message =>
              respondWithMediaType(mediaType) {
                requestExecute(message, preguntasConfrontacionActor)
              }
          }
        }
      }
    }
  }
}
