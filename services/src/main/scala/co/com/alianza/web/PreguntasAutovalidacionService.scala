package co.com.alianza.web

import co.com.alianza.app.{ AlianzaCommons, CrossHeaders }
import co.com.alianza.infrastructure.dto.security.UsuarioAuth
import co.com.alianza.infrastructure.messages._
import spray.routing.{ Directives }

/**
 *
 * @author seven4n
 */
class PreguntasAutovalidacionService extends Directives with AlianzaCommons with CrossHeaders {

  import co.com.alianza.infrastructure.messages.PreguntasAutovalidacionMessagesJsonSupport._

  def route(user: UsuarioAuth) = {
    pathPrefix("preguntasAutovalidacion") {
      get {
        respondWithMediaType(mediaType) {
          pathPrefix("comprobar") {
            requestExecute(new ObtenerPreguntasComprobarMessage(user.id, user.tipoCliente), preguntasAutovalidacionActor)
          } ~ {
            requestExecute(new ObtenerPreguntasMessage, preguntasAutovalidacionActor)
          }
        }
      } ~ put {
        respondWithMediaType(mediaType) {
          {
            entity(as[RespuestasMessage]) {
              message: RespuestasMessage =>
                val guardarMessage = GuardarRespuestasMessage(user.id, user.tipoCliente, message.respuestas)
                requestExecute(guardarMessage, preguntasAutovalidacionActor)
            }
          }
        }
      } ~ post {
        respondWithMediaType(mediaType) {
          pathPrefix("comprobar") {
            entity(as[RespuestasComprobacionMessage]) {
              message: RespuestasComprobacionMessage =>
                val validacionMessage = ValidarRespuestasMessage(user.id, user.tipoCliente, message.respuestas, message.numeroIntentos)
                requestExecute(validacionMessage, preguntasAutovalidacionActor)
            }
          }
        }
      } ~ delete {
        respondWithMediaType(mediaType) {
          pathPrefix("comprobar") {
            requestExecute(new BloquearRespuestasMessage(user.id, user.tipoCliente), preguntasAutovalidacionActor)
          }
        }
      }
    }
  }
}
