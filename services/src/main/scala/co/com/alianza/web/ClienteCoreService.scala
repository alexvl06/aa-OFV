package co.com.alianza.web

import akka.actor.{ ActorSelection, ActorSystem }
import spray.routing.Directives
import co.com.alianza.app.AlianzaCommons
import co.com.alianza.infrastructure.messages.{ ExisteClienteCoreMessage, ExisteClienteCoreMessagesJsonSupport }

import scala.concurrent.ExecutionContext

/**
 *
 * @author smontanez
 */
case class ClienteCoreService(consultaClienteActor: ActorSelection)(implicit val system: ActorSystem) extends Directives with AlianzaCommons {

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
