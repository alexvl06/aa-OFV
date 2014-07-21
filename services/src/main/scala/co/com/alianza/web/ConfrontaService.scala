package co.com.alianza.web

import spray.routing.Directives
import co.com.alianza.app.AlianzaCommons
import co.com.alianza.infrastructure.messages.{ValidarCuestionarioRequestMessage, ObtenerCuestionarioAdicionalRequestMessage, ObtenerCuestionarioRequestMessage, InboxMessage}
import spray.http.StatusCodes._

class ConfrontaService extends Directives with AlianzaCommons  {
  import co.com.alianza.infrastructure.messages.ConfrontaMessagesJsonSupport._

  val confronta = "confronta"
  val obtenerCuestionario = "obtenerCuestionario"
  val obtenerCuestionarioAdicional = "obtenerCuestionarioAdicional"
  val validarCuestionario = "validarCuestionario"

  def route= {
    path(confronta/obtenerCuestionario) {
      post {
        respondWithMediaType(mediaType) {
          entity(as[ObtenerCuestionarioRequestMessage]) {
            message =>
              requestExecute(message, confrontaActor)
          }
        }
      }
    } ~ path(confronta/obtenerCuestionarioAdicional) {
      post {
        respondWithMediaType(mediaType) {
          entity(as[ObtenerCuestionarioAdicionalRequestMessage]) {
            message =>
              requestExecute(message, confrontaAditionalActor)
          }
        }
      }
    }~ path(confronta/validarCuestionario) {
      post {
        respondWithMediaType(mediaType) {
          entity(as[ValidarCuestionarioRequestMessage]) {
            message =>
              requestExecute(message, confrontaValidationActor)
          }
        }
      }
    }
  }

}