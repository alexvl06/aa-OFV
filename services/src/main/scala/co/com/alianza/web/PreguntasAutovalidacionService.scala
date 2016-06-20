package co.com.alianza.web

import co.com.alianza.app.{ AlianzaCommons, CrossHeaders }
import co.com.alianza.infrastructure.auditing.AuditingHelper
import co.com.alianza.infrastructure.auditing.AuditingHelper._
import co.com.alianza.infrastructure.dto.security.UsuarioAuth
import co.com.alianza.infrastructure.messages.PinMessages.CambiarContrasena
import co.com.alianza.infrastructure.messages._
import spray.routing.{ RequestContext, Directives }

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
                clientIP {
                  ip =>
                    val validacionMessage = ValidarRespuestasMessage(user.id, user.tipoCliente, message.respuestas, message.numeroIntentos)
                    mapRequestContext((r: RequestContext) => requestWithAuiditing(r, AuditingHelper.fiduciariaTopic,
                      AuditingHelper.autovalidacionComprobarIndex, ip.value, kafkaActor, message)) {
                      requestExecute(validacionMessage, preguntasAutovalidacionActor)
                    }
                }
            }
          }
        }
      } ~ delete {
        respondWithMediaType(mediaType) {
          pathPrefix("comprobar") {
            clientIP {
              ip =>
                val message = new BloquearRespuestasMessage(user.id, user.tipoCliente)
                mapRequestContext((r: RequestContext) => requestWithAuiditing(r, AuditingHelper.fiduciariaTopic,
                  AuditingHelper.autovalidacionBloquearIndex, ip.value, kafkaActor, message)) {
                  requestExecute(message, preguntasAutovalidacionActor)
                }
            }
          }
        }
      }
    }
  }
}
