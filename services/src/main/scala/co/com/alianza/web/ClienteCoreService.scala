package co.com.alianza.web

import spray.routing.Directives
import co.com.alianza.app.AlianzaCommons
import co.com.alianza.infrastructure.messages.{ExisteClienteCoreMessagesJsonSupport, ExisteClienteCoreMessage}

/**
 *
 * @author smontanez
 */
class ClienteCoreService extends Directives with AlianzaCommons {

  import ExisteClienteCoreMessagesJsonSupport._

  def route = {
   get {
     path("existeClienteCoreAlianza" / IntNumber / IntNumber)  {
       (tipoDocumento, numDocumento) =>
          respondWithMediaType(mediaType) {
                requestExecute(ExisteClienteCoreMessage(tipoDocumento, numDocumento.toString), consultaClienteActor)
            }
          }
      }
   }
}
