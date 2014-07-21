package co.com.alianza.domain.aggregates.confronta

import akka.actor.{Props, ActorLogging, Actor}
import akka.routing.RoundRobinPool
import co.com.alianza.app.AlianzaActors
import com.asobancaria.cifin.cifin.confrontav2plusws.services.ConfrontaBasicoPlusWS.ConfrontaBasicoPlusWebServiceServiceLocator

/**
 * Created by ricardoseven on 6/06/14.
 */
class ConfrontaAditionalActorSupervisor extends Actor with ActorLogging {
  import akka.actor.SupervisorStrategy._
  import akka.actor.OneForOneStrategy

  val confrontaAditionalActor = context.actorOf(Props[ConfrontaAditionalActor].withRouter(RoundRobinPool(nrOfInstances = 1)), "confrontaAditionalActor")

  def receive = {

    case message: Any =>
      confrontaAditionalActor forward message

  }

  override val supervisorStrategy = OneForOneStrategy() {
    case exception: Exception =>
      exception.printStackTrace()
      log.error(exception, exception.getMessage)
      Restart
  }
}

class ConfrontaAditionalActor extends Actor with ActorLogging with AlianzaActors {
  import scala.concurrent.ExecutionContext
  implicit val _: ExecutionContext = context.dispatcher
  import co.com.alianza.util.json.MarshallableImplicits._

  def receive = {
    case message  =>

      val currentSender = sender()
      val locator =  new ConfrontaBasicoPlusWebServiceServiceLocator
      val response = locator.getConfrontaBasicoPlusWS(new java.net.URL("http://localhost:8088/mockConfrontaBasicoPlusWSSoapBinding")).obtenerCuestionarioAdicional(null,null);
      currentSender ! response.toString
    /*val result = DataAccessAdapter.obtenerUsuarios()


    result  onComplete {
      case Failure(failure)  =>    currentSender ! failure
      case Success(value)    =>
        value match {
          case zSuccess(response: Vector[Usuario]) =>  currentSender ! response.toJson
          case zFailure(error)    =>                   currentSender ! error
        }
    }*/

  }

}