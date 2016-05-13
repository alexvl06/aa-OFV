package co.com.alianza.web

import co.com.alianza.infrastructure.messages.InboxMessage
import spray.routing.Directives
import co.com.alianza.app.AlianzaCommons

/**
 * Created by david on 16/06/14.
 */
class ReglasContrasenasService extends Directives with AlianzaCommons {

  val reglasContrasenas = "reglasContrasenas"

  def route = {

    path(reglasContrasenas) {
      get {
        respondWithMediaType(mediaType) {
          requestExecute(InboxMessage(), contrasenasActor)
        }
      }
    }
  }

}
