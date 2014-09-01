package co.com.alianza.domain.aggregates.confronta

import akka.actor.{ActorRef, Actor, Props, ActorLogging}
import scalaz.{Failure => zFailure, Success => zSuccess}
import scala.util.{Success, Failure}
import co.com.alianza.app.AlianzaActors
import com.typesafe.config.{ConfigFactory, Config}
import com.asobancaria.tciweb1.cifin.confrontav2plusws.services.ConfrontaUltraWS.ConfrontaUltraWebServiceServiceLocator
import co.cifin.confrontaultra.dto.ultra._
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
            message.codigoTipoIdentificacion.toString,
            message.fechaExpedicion));*/

      val resouesta11 = new OpcionRespuestaPreguntaULTRADTO(1,1,"VISA")
      val resouesta12 = new OpcionRespuestaPreguntaULTRADTO(1,2,"AMERICAN EXPRESS")
      val resouesta13 = new OpcionRespuestaPreguntaULTRADTO(1,3,"MASTER CARD")
      val resouesta14 = new OpcionRespuestaPreguntaULTRADTO(1,4,"NINGUNA DE LAS ANTERIORES")

      val resouesta21 = new OpcionRespuestaPreguntaULTRADTO(2,1,"NINGUNO")
      val resouesta22 = new OpcionRespuestaPreguntaULTRADTO(2,2,"UNO")
      val resouesta23 = new OpcionRespuestaPreguntaULTRADTO(2,3,"DOS")
      val resouesta24 = new OpcionRespuestaPreguntaULTRADTO(2,4,"TRES o MÁS")

      val resouesta31 = new OpcionRespuestaPreguntaULTRADTO(3,1,"SI")
      val resouesta32 = new OpcionRespuestaPreguntaULTRADTO(3,2,"NO")


      val resouesta41 = new OpcionRespuestaPreguntaULTRADTO(4,1,"CUENTA CORRIENTE")
      val resouesta42 = new OpcionRespuestaPreguntaULTRADTO(4,2,"TARJETA DE CREDITO")
      val resouesta43 = new OpcionRespuestaPreguntaULTRADTO(4,3,"CUENTA CORRIENTE Y TARJETA DE CREDITO")
      val resouesta44 = new OpcionRespuestaPreguntaULTRADTO(4,4,"NINGUNO DE LOS ANTERIORES")

      val resouesta51 = new OpcionRespuestaPreguntaULTRADTO(5,1,"NARIÑO")
      val resouesta52 = new OpcionRespuestaPreguntaULTRADTO(5,2,"QUINDIO")
      val resouesta53 = new OpcionRespuestaPreguntaULTRADTO(5,3,"BOGOTA DISTRITO CA")
      val resouesta54 = new OpcionRespuestaPreguntaULTRADTO(5,4,"NINGUNA DE LAS ANTERIORES")

      val listadoRespuestas: Array[OpcionRespuestaPreguntaULTRADTO] = Array(resouesta11,resouesta12,resouesta13,resouesta14)
      val listadoRespuestas2: Array[OpcionRespuestaPreguntaULTRADTO] = Array(resouesta21,resouesta22,resouesta23,resouesta24)
      val listadoRespuestas3: Array[OpcionRespuestaPreguntaULTRADTO] = Array(resouesta31,resouesta32)
      val listadoRespuestas4: Array[OpcionRespuestaPreguntaULTRADTO] = Array(resouesta41,resouesta42,resouesta43,resouesta44)
      val listadoRespuestas5: Array[OpcionRespuestaPreguntaULTRADTO] = Array(resouesta51,resouesta52,resouesta53,resouesta54)

      val pregunta1 = new PreguntaULTRADTO(1,listadoRespuestas,1,"¿CUÁL O CUÁLES DE LOS SIGUIENTES PRODUCTOS TIENE CON COLPATRIA?")
      val pregunta2 = new PreguntaULTRADTO(2,listadoRespuestas2,2,"¿DE QUÉ MARCA ES SU TARJETA DE CRÉDITO CON BANCO DE BOGOTA?")
      val pregunta3 = new PreguntaULTRADTO(3,listadoRespuestas3,3,"¿CUÁL ES EL DEPARTAMENTO EN DONDE FUE EXPEDIDO SU DOCUMENTO DE IDENTIDAD?")
      val pregunta4 = new PreguntaULTRADTO(4,listadoRespuestas4,4,"¿USTED FUE BENEFICIARIO DEL FONDO DE RESERVA (FRECH) PARA LA ADQUISICIÓN DE VIVIENDA QUE OTORGÓ EL GOBIERNO?")
      val pregunta5 = new PreguntaULTRADTO(5,listadoRespuestas5,5,"ACTUALMENTE CUANTOS CREDITOS TIENE CON FEDEABC - FONDO DE EMPLEADOS  DE  ASOBANCARIA?")

      val listadoPreguntas: Array[PreguntaULTRADTO] = Array(pregunta1,pregunta2,pregunta3,pregunta4,pregunta5)
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
      val locator =  new ConfrontaUltraWebServiceServiceLocator()
      message.respuestas
      val respuestas = new java.util.ArrayList[RespuestaPreguntaULTRADTO]()
      for(res <- message.respuestas){
        val split = res.split(",")
        respuestas.add(new RespuestaPreguntaULTRADTO(split(0).toInt,split(1).toInt))
      }

      /*val response = locator.getConfrontaUltraWS(new java.net.URL(config.getString("confronta.service.evaluarCuestionario.location"))).evaluarCuestionario(new ParametrosSeguridadULTRADTO(config.getString("confronta.service.claveCIFIN"),config.getString("confronta.service.password"))
        ,new RespuestaCuestionarioULTRADTO(message.secuenciaCuestionario,message.codigoCuestionario,respuestas.toArray(new Array[RespuestaPreguntaULTRADTO](respuestas.size()))))*/

      val aciertos = 5
      val claveCIFIN = "passwordTest"
      val codigoCuestionario = 7050
      val respuestaProceso: RespuestaULTRADTO = new RespuestaULTRADTO(1,"Cuestionario Obtenido Exitosamente")
      val resultadoConfrontacion: Int = 1
      val resultadoScore: Int = -1
      val response = new ResultadoEvaluacionCuestionarioULTRADTO(aciertos,claveCIFIN,codigoCuestionario,respuestaProceso,resultadoConfrontacion,resultadoScore)

      if(resultadoConfrontacion == 1){
        actualizarEstadoConfronta(message.id,response,currentSender)
      } else {
        currentSender ! response.toJson
      }

  }

  private def actualizarEstadoConfronta(numeroIdentificacion: String, response:ResultadoEvaluacionCuestionarioULTRADTO, currentSender: ActorRef) = {
    val resultActualizarEstadoConfronta = co.com.alianza.infrastructure.anticorruption.usuarios.DataAccessAdapter.actualizarEstadoConfronta(numeroIdentificacion)
    resultActualizarEstadoConfronta onComplete {
      case Failure(failure) => currentSender ! failure
      case Success(value) =>
        value match {
          case zSuccess(code: Int) =>
          currentSender !  response.toJson
          case zFailure(error) =>
            currentSender ! error
        }
    }
  }

}