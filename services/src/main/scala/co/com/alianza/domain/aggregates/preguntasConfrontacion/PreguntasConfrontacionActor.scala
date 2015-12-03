package co.com.alianza.domain.aggregates.preguntasConfrontacion

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.routing.RoundRobinPool
import co.com.alianza.app.{AlianzaActors, MainActors}
import co.com.alianza.infrastructure.messages.{GuardarRespuestasMessage, ObtenerPreguntasMessage, ObtenerCuestionarioAdicionalRequestMessage}
import com.typesafe.config.Config


class PreguntasConfrontacionSupervisor extends Actor with ActorLogging {
  import akka.actor.OneForOneStrategy
  import akka.actor.SupervisorStrategy._

  val preguntasConfrontacionActor = context.actorOf(Props[PreguntasConfrontacionActor].withRouter(RoundRobinPool(nrOfInstances = 2)), "preguntasConfrontacionActor")

  def receive = {
    case message: Any =>
      preguntasConfrontacionActor forward message
  }

  override val supervisorStrategy = OneForOneStrategy() {
    case exception: Exception =>
      exception.printStackTrace()
      log.error(exception, exception.getMessage)
      Restart
  }
}


class PreguntasConfrontacionActor extends Actor with ActorLogging with AlianzaActors {
  import scala.concurrent.ExecutionContext
  implicit val _: ExecutionContext = context.dispatcher
  private val config: Config = MainActors.conf


  def receive = {
    case message:ObtenerPreguntasMessage =>
      println("AQUI!")

    case message:GuardarRespuestasMessage =>
      println("AQUI!")
  }
}
