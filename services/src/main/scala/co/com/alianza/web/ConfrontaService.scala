package co.com.alianza.web

import akka.actor.ActorSelection
import spray.routing.Directives
import co.com.alianza.app.AlianzaCommons
import co.com.alianza.infrastructure.messages.{ ObtenerCuestionarioAdicionalRequestMessage, ObtenerCuestionarioRequestMessage, ValidarCuestionarioDesbloqueoRequestMessage, ValidarCuestionarioRequestMessage }
import spray.http.StatusCodes._
import co.com.alianza.util.json.JsonUtil

import scala.concurrent.ExecutionContext

case class ConfrontaService(confrontaActor: ActorSelection)(implicit val ec: ExecutionContext) extends Directives with AlianzaCommons {
  import co.com.alianza.infrastructure.messages.ConfrontaMessagesJsonSupport._

  val confronta = "confronta"
  val obtenerCuestionario = "obtenerCuestionario"
  val obtenerCuestionarioAdicional = "obtenerCuestionarioAdicional"
  val validarCuestionario = "validarCuestionario"
  val validarCuestionarioDesbloqueo = "validarCuestionarioDesbloqueo"

  def route = {
    path(confronta / obtenerCuestionarioAdicional) {
      post {
        respondWithMediaType(mediaType) {
          entity(as[ObtenerCuestionarioAdicionalRequestMessage]) {
            message =>
              requestExecute(message, confrontaActor)
          }
        }
      }
    } ~ path(confronta / validarCuestionario) {
      post {
        entity(as[ValidarCuestionarioRequestMessage]) {
          message =>
            respondWithMediaType(mediaType) {
              requestExecute(message, confrontaActor)
            }
        }
      }
    } ~ path(confronta / validarCuestionarioDesbloqueo) {
      post {
        entity(as[ValidarCuestionarioDesbloqueoRequestMessage]) {
          message =>
            respondWithMediaType(mediaType) {
              requestExecute(message, confrontaActor)
            }
        }
      }
    }
  }

}
