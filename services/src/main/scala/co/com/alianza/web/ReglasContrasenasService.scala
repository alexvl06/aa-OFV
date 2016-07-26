package co.com.alianza.web

import akka.actor.{ ActorSelection, ActorSystem }
import co.com.alianza.infrastructure.messages.InboxMessage
import spray.routing.Directives
import co.com.alianza.app.AlianzaCommons

import scala.concurrent.ExecutionContext

/**
 * Created by david on 16/06/14.
 */
case class ReglasContrasenasService(contrasenasActor: ActorSelection) extends Directives with AlianzaCommons {

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
