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
            requestExecute(new ObtenerPreguntasRandomMessage(Some(user.id), user.tipoCliente), preguntasAutovalidacionActor)
          } ~ {
            requestExecute(new ObtenerPreguntasMessage, preguntasAutovalidacionActor)
          }
        }
      } ~ put {
        respondWithMediaType(mediaType) {
          {
            entity(as[GuardarRespuestasMessage]) {
              message =>
                requestExecute(message.copy(idUsuario = Some(user.id), tipoCliente = Some(user.tipoCliente)), preguntasAutovalidacionActor)
            }
          }
        }
      } ~ post {
        respondWithMediaType(mediaType) {
          pathPrefix("comprobar") {
            entity(as[RespuestasMessage]) {
              message =>
                val message = ValidarRespuestasMessage(user.id, user.tipoCliente, message.respuestas)
                requestExecute(message.copy(idUsuario = Some(user.id), tipoCliente = Some(user.tipoCliente)), preguntasAutovalidacionActor)
            }
          }
        }
      } ~ delete {
        respondWithMediaType(mediaType) {
          pathPrefix("comprobar") {
            requestExecute(new BloquearRespuestasMessage(Some(user.id), user.tipoCliente), preguntasAutovalidacionActor)
          }
        }
      }
    }
  }
}
