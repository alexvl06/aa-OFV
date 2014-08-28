package co.com.alianza.domain.aggregates.web

import akka.actor.Actor
import akka.actor.ActorSelection
import akka.actor.ActorLogging
import akka.actor.ActorSelection.toScala
import akka.actor.actorRef2Scala
import co.com.alianza.infrastructure.messages.{ResponseMessage, MessageService}
import co.com.alianza.exceptions.{TimeoutLevel, PersistenceException, AlianzaException}

class AnonymousActor(actorService: ActorSelection) extends Actor with ActorLogging {


  var response:Option[Any] = None

  //Define el numero de reintentos
  var reintentos = 3

  var message = Option.empty[MessageService]

  /**
   * preRestar encargado de tomar el mensaje que no se pudo procesar y respecto a los criterios de suspervición reintenta
   * la acción
   */
  override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
    log.info("Throwable '{}'", reason.getStackTrace)
    log.info("Restarting PageParsingActor and resending message '{}'", message)
    if (message.isDefined && message.nonEmpty) {
      self.forward(message.get)
    }
    super.preRestart(reason, message)
  }

  def receive = {
    case m: MessageService =>
      log.info(m.toString)
      message = Option(m)
      actorService ! m
      context.become(waitingResponses)

  }
  //TODO:Agregar logs
  def waitingResponses: Receive = {

    case res: String =>
      response = Some(res)
      replyIfReady()

    case res: ResponseMessage =>
      response = Some(res)
      replyIfReady()


    case error: AlianzaException =>
      error.level match {
        case TimeoutLevel =>
          if(!retryRequest) throw error
        case _ =>  throw error
      }

    case x: Exception =>
      log.error(x,"")
      log.info("Num: de reintentos:" + reintentos)
      throw x

    case w: Any => context.parent ! w
  }

  def replyIfReady() =
    if (response.nonEmpty) {
      context.parent ! response
    }


  private def retryRequest = {
    if (reintentos > 0) {
      log.info(s"Reintendando petición. Contador reintentos : $reintentos")
      reintentos = reintentos - 1
      actorService ! message.get
      true
    } else false
  }
}