package co.com.alianza.domain.aggregates.autoregistro

import akka.actor.{ActorLogging, Actor}
import co.com.alianza.infrastructure.anticorruption.clientes.DataAccessAdapter
import scalaz.{Failure => zFailure, Success => zSuccess}
import scala.util.{Success, Failure}
import co.com.alianza.infrastructure.messages.{ResponseMessage, ExisteClienteCoreMessage}
import co.com.alianza.infrastructure.dto.Cliente
import spray.http.StatusCodes._



import akka.actor.Props
import akka.routing.RoundRobinPool




class ConsultaClienteActorSupervisor extends Actor with ActorLogging {
  import akka.actor.SupervisorStrategy._
  import akka.actor.OneForOneStrategy

  val consultaClienteActor = context.actorOf(Props[ConsultaClienteActor].withRouter(RoundRobinPool(nrOfInstances = 1)), "consultaClienteActor")

  def receive = {

    case message: Any =>
      consultaClienteActor forward message

  }

  override val supervisorStrategy = OneForOneStrategy() {
    case exception: Exception =>
      exception.printStackTrace()
      log.error(exception, exception.getMessage)
      Restart
  }

}
/**
 *
 * @author smontanez
 */
class ConsultaClienteActor extends Actor with ActorLogging{

  import scala.concurrent.ExecutionContext
  implicit val _: ExecutionContext = context.dispatcher
  import co.com.alianza.util.json.MarshallableImplicits._

  def receive = {
    case message:ExisteClienteCoreMessage  =>

      val currentSender = sender()
      val result = DataAccessAdapter.consultarCliente(message.numDocumento)

      result  onComplete {
        case Failure(failure)  =>    currentSender ! failure
        case Success(value)    =>
          value match {
            case zSuccess(response:Option[Cliente])   =>
              response match {
                case Some(valueResponse)  =>  currentSender ! valueResponse.toJson
                case None                 =>  currentSender ! ResponseMessage(NotFound)
              }
            case zFailure(error)              =>  currentSender ! error
          }
      }
  }

}