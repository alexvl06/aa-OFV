package co.com.alianza.domain.aggregates.confronta

import akka.actor.{ Actor, Props, ActorLogging }
import akka.routing.RoundRobinPool
import scalaz.{Failure => zFailure, Success => zSuccess}
import scala.util.{Success, Failure}
import co.com.alianza.app.AlianzaActors
import com.typesafe.config.{ConfigFactory, Config}
import com.asobancaria.tciweb1.cifin.confrontav2plusws.services.ConfrontaUltraWS.ConfrontaUltraWebServiceServiceLocator
import co.cifin.confrontaultra.dto.ultra.{ParametrosULTRADTO,ParametrosSeguridadULTRADTO}
import co.com.alianza.infrastructure.messages.{ValidarCuestionarioRequestMessage, ObtenerCuestionarioAdicionalRequestMessage, ObtenerCuestionarioRequestMessage}


class ConfrontaActorSupervisor extends Actor with ActorLogging {
  import akka.actor.SupervisorStrategy._
  import akka.actor.OneForOneStrategy

  val confrontaActor = context.actorOf(Props[ConfrontaActor].withRouter(RoundRobinPool(nrOfInstances = 1)), "confrontaActor")

  def receive = {

    case message: Any =>
      confrontaActor forward message

  }

  override val supervisorStrategy = OneForOneStrategy() {
    case exception: Exception =>
      exception.printStackTrace()
      log.error(exception, exception.getMessage)
      Restart
  }
}


class ConfrontaActor extends Actor with ActorLogging with AlianzaActors {
  import scala.concurrent.ExecutionContext
  implicit val _: ExecutionContext = context.dispatcher
  import co.com.alianza.util.json.MarshallableImplicits._
  private val config: Config = ConfigFactory.load


  def receive = {
    case message:ObtenerCuestionarioRequestMessage  =>

      val currentSender = sender()
      val locator =  new ConfrontaUltraWebServiceServiceLocator()
      val response = locator.getConfrontaUltraWS(new java.net.URL(config.getString("confronta.service.obtenerCuestionario.location"))).
        obtenerCuestionario(new ParametrosSeguridadULTRADTO(config.getString("confronta.service.claveCIFIN"),config.getString("confronta.service.password")),
          new ParametrosULTRADTO(
            message.numeroIdentificacion,
            0,
            config.getInt("confronta.service.cuestionario"),
            "",
            0,
            message.primerApellido,
            message.codigoTipoIdentificacion,
            message.fechaExpedicion));
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
    case message:ObtenerCuestionarioAdicionalRequestMessage =>
      val currentSender = sender()
      //val locator =  new ConfrontaBasicoPlusWebServiceServiceLocator
      //val response = locator.getConfrontaBasicoPlusWS(new java.net.URL(config.getString("confronta.service.obtenerCuestionarioAdicional.location"))).obtenerCuestionario(null,null);
      //currentSender ! response.toString

    case message:ValidarCuestionarioRequestMessage =>
      val currentSender = sender()
      //val locator =  new ConfrontaBasicoPlusWebServiceServiceLocator
      //val response = locator.getConfrontaBasicoPlusWS(new java.net.URL(config.getString("confronta.service.evaluarCuestionario.location"))).obtenerCuestionario(null,null);
      //currentSender ! response.toString
  }

}