package co.com.alianza.web

import spray.routing.Directives
import co.com.alianza.app.AlianzaCommons
import co.com.alianza.infrastructure.messages.{ObtenerCuestionarioAdicionalRequestMessage, ObtenerCuestionarioRequestMessage,ValidarCuestionarioRequestMessage}
import spray.http.StatusCodes._
import co.com.alianza.util.json.JsonUtil

class ConfrontaService extends Directives with AlianzaCommons  {
  import co.com.alianza.infrastructure.messages.ConfrontaMessagesJsonSupport._

  val confronta = "confronta"
  val obtenerCuestionario = "obtenerCuestionario"
  val obtenerCuestionarioAdicional = "obtenerCuestionarioAdicional"
  val validarCuestionario = "validarCuestionario"

  def route= {
    path(confronta/obtenerCuestionarioAdicional) {
      post {
        respondWithMediaType(mediaType) {
          entity(as[ObtenerCuestionarioAdicionalRequestMessage]) {
            message =>
              requestExecute(message, confrontaActor)
          }
        }
      }
    }~ path(confronta/validarCuestionario) {
      post {
        entity(as[ValidarCuestionarioRequestMessage]) {
          message =>
            println(JsonUtil.toJson(message))
          respondWithMediaType(mediaType) {
            requestExecute(message, confrontaActor)
          }
        }
      }
    }
  }

}