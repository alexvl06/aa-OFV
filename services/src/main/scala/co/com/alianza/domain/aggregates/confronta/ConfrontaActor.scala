package co.com.alianza.domain.aggregates.confronta

import akka.actor.{ Actor, Props, ActorLogging }
import akka.routing.RoundRobinPool
import scalaz.{Failure => zFailure, Success => zSuccess}
import scala.util.{Success, Failure}
import co.com.alianza.app.AlianzaActors
import com.typesafe.config.{ConfigFactory, Config}
import com.asobancaria.tciweb1.cifin.confrontav2plusws.services.ConfrontaUltraWS.ConfrontaUltraWebServiceServiceLocator
import co.cifin.confrontaultra.dto.ultra._
import co.com.alianza.infrastructure.messages.{ValidarCuestionarioRequestMessage, ObtenerCuestionarioAdicionalRequestMessage, ObtenerCuestionarioRequestMessage}
import co.com.alianza.infrastructure.messages.ObtenerCuestionarioAdicionalRequestMessage
import akka.routing.RoundRobinPool
import co.com.alianza.infrastructure.messages.ObtenerCuestionarioRequestMessage
import co.com.alianza.infrastructure.messages.ValidarCuestionarioRequestMessage


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
      /*val response = locator.getConfrontaUltraWS(new java.net.URL(config.getString("confronta.service.obtenerCuestionario.location"))).
        obtenerCuestionario(new ParametrosSeguridadULTRADTO(config.getString("confronta.service.claveCIFIN"),config.getString("confronta.service.password")),
          new ParametrosULTRADTO(
            message.numeroIdentificacion,
            0,
            config.getInt("confronta.service.cuestionario"),
            "",
            0,
            message.primerApellido,
            message.codigoTipoIdentificacion,
            message.fechaExpedicion));*/
      val resouesta11 = new OpcionRespuestaPreguntaULTRADTO(1,1,"Lo se pero no quiero decirlo")
      val resouesta12 = new OpcionRespuestaPreguntaULTRADTO(2,2,"No importa")
      val resouesta13 = new OpcionRespuestaPreguntaULTRADTO(3,3,"Solo Dios sabe")
      val resouesta14 = new OpcionRespuestaPreguntaULTRADTO(3,3,"Todas las anteriores")
      val listadoRespuestas: Array[OpcionRespuestaPreguntaULTRADTO] = Array(resouesta11,resouesta12,resouesta13,resouesta14)
      val pregunta1 = new PreguntaULTRADTO(1,listadoRespuestas,1,"Cual es el significado de la vida?")
      val pregunta2 = new PreguntaULTRADTO(2,listadoRespuestas,2,"Cual es el origen del universo?")
      val listadoPreguntas: Array[PreguntaULTRADTO] = Array(pregunta1,pregunta2)
      val huella: HuellaULTRADTO = new HuellaULTRADTO("Entidad",3,"fechaConsulta","resultadoConsulta")
      val huellaConsulta: Array[HuellaULTRADTO] = Array(huella)
      val response = new CuestionarioULTRADTO(listadoPreguntas,123,"claveCIFIN",new RespuestaULTRADTO(),new TerceroULTRADTO(),123456,huellaConsulta,"Cuestionario Super Cool").toJson
      currentSender ! response

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