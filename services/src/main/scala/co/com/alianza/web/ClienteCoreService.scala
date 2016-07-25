package co.com.alianza.web

import akka.actor.ActorSelection
import spray.routing.Directives
import co.com.alianza.app.AlianzaCommons
import co.com.alianza.infrastructure.messages.{ ExisteClienteCoreMessage, ExisteClienteCoreMessagesJsonSupport }

/**
 *
 * @author smontanez
 */
case class ClienteCoreService(consultaClienteActor: ActorSelection) extends Directives with AlianzaCommons {

  import ExisteClienteCoreMessagesJsonSupport._

  def route = {
    get {
      path("existeClienteCoreAlianza" / IntNumber / IntNumber) {
        (tipoDocumento, numDocumento) =>
          respondWithMediaType(mediaType) {
            requestExecute(ExisteClienteCoreMessage(tipoDocumento, numDocumento.toString), consultaClienteActor)
          }
      }
    }
  }
}
