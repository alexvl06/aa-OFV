package co.com.alianza.domain.aggregates.confronta

import akka.actor.{ActorRef, Actor, Props, ActorLogging}
import scalaz.{Failure => zFailure, Success => zSuccess}
import scala.util.{Success, Failure}
import co.com.alianza.app.AlianzaActors
import com.typesafe.config.{ConfigFactory, Config}
import co.cifin.confrontaultra.dto.ultra._
import co.com.alianza.infrastructure.messages.{UsuarioMessage, ObtenerCuestionarioAdicionalRequestMessage, ObtenerCuestionarioRequestMessage, ValidarCuestionarioRequestMessage}
import akka.routing.RoundRobinPool
import com.asobancaria.cifinpruebas.cifin.confrontav2plusws.services.ConfrontaUltraWS.{ConfrontaUltraWSSoapBindingStub, ConfrontaUltraWebServiceServiceLocator}
import co.com.alianza.util.json.JsonUtil
import co.cifin.confrontaultra.dto.ultras.RespuestaCuestionarioULTRADTO
import co.com.alianza.infrastructure.anticorruption.usuarios.{DataAccessAdapter => DataAccessAdapterUsuario }
import co.com.alianza.util.clave.Crypto
import co.com.alianza.domain.aggregates.usuarios.ErrorPersistence
import co.com.alianza.persistence.entities.PerfilUsuario
import enumerations.PerfilesUsuario


class ConfrontaActorSupervisor extends Actor with ActorLogging {
  import akka.actor.SupervisorStrategy._
  import akka.actor.OneForOneStrategy

  val confrontaActor = context.actorOf(Props[ConfrontaActor].withRouter(RoundRobinPool(nrOfInstances = 2)), "confrontaActor")

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

      val locator: ConfrontaUltraWebServiceServiceLocator = new ConfrontaUltraWebServiceServiceLocator(config.getString("confronta.service.obtenerCuestionario.location"))
      val stub: ConfrontaUltraWSSoapBindingStub = locator.getConfrontaUltraWS.asInstanceOf[ConfrontaUltraWSSoapBindingStub]
      val parametros: ParametrosSeguridadULTRADTO = new ParametrosSeguridadULTRADTO
      parametros.setClaveCIFIN(config.getString("confronta.service.claveCIFIN"))
      parametros.setPassword(config.getString("confronta.service.password"))
      val parametrosUltra: ParametrosULTRADTO = new ParametrosULTRADTO
      parametrosUltra.setCodigoDepartamento(config.getInt("confronta.service.departamento"))
      parametrosUltra.setCodigoCuestionario(config.getInt("confronta.service.cuestionario"))
      parametrosUltra.setTelefono("");
      parametrosUltra.setCodigoCiudad(config.getInt("confronta.service.ciudad"))
      parametrosUltra.setPrimerApellido(message.primerApellido.toUpperCase())
      parametrosUltra.setCodigoTipoIdentificacion(if(message.codigoTipoIdentificacion.toString.equals("1")){"1"}else{"3"})
      parametrosUltra.setNumeroIdentificacion(message.numeroIdentificacion)
      parametrosUltra.setFechaExpedicion(message.fechaExpedicion)

      val response: CuestionarioULTRADTO = stub.obtenerCuestionario(parametros, parametrosUltra)
      currentSender ! JsonUtil.toJson(response)

    case message:ObtenerCuestionarioAdicionalRequestMessage =>
      val currentSender = sender()
      //val locator =  new ConfrontaBasicoPlusWebServiceServiceLocator
      //val response = locator.getConfrontaBasicoPlusWS(new java.net.URL(config.getString("confronta.service.obtenerCuestionarioAdicional.location"))).obtenerCuestionario(null,null);
      //currentSender ! response.toString

    case message:ValidarCuestionarioRequestMessage =>
      val currentSender = sender()
      val respuestas = new java.util.ArrayList[RespuestaPreguntaULTRADTO]()
      for(res <- message.respuestas){
        val split = res.split(",")
        respuestas.add(new RespuestaPreguntaULTRADTO(split(0).toInt,split(1).toInt))
      }

      val locator: ConfrontaUltraWebServiceServiceLocator = new ConfrontaUltraWebServiceServiceLocator(config.getString("confronta.service.obtenerCuestionario.location"))
      val stub: ConfrontaUltraWSSoapBindingStub = locator.getConfrontaUltraWS.asInstanceOf[ConfrontaUltraWSSoapBindingStub]
      val parametros: ParametrosSeguridadULTRADTO = new ParametrosSeguridadULTRADTO
      parametros.setClaveCIFIN(config.getString("confronta.service.claveCIFIN"))
      parametros.setPassword(config.getString("confronta.service.password"))
      val parametrosUltra: RespuestaCuestionarioULTRADTO = new RespuestaCuestionarioULTRADTO(message.secuenciaCuestionario,message.codigoCuestionario,respuestas.toArray(new Array[RespuestaPreguntaULTRADTO](respuestas.size())))
      val response = stub.evaluarCuestionario(parametros,parametrosUltra)

      if(response.getRespuestaProceso.getCodigoRespuesta == 1){
        actualizarEstadoConfronta(message.id,response,currentSender)
      } else {
        currentSender ! response.toJson
      }

  }

  private def actualizarEstadoConfronta(message: UsuarioMessage, response:ResultadoEvaluacionCuestionarioULTRADTO, currentSender: ActorRef) = {
    val resultActualizarEstadoConfronta = DataAccessAdapterUsuario.crearUsuario(message.toEntityUsuario( Crypto.hashSha256(message.contrasena))).map(_.leftMap( pe => ErrorPersistence(pe.message,pe)))
    resultActualizarEstadoConfronta onComplete {
      case Failure(failure) =>
        currentSender ! failure
      case Success(value) =>
        value match {
          case zSuccess(code: Int) =>
            currentSender !  response.toJson
            DataAccessAdapterUsuario.asociarPerfiles(PerfilUsuario(code,PerfilesUsuario.clienteIndividual.id)::Nil)
            if(message.activarIP && message.clientIp.isDefined){
              DataAccessAdapterUsuario.relacionarIp(code,message.clientIp.get)
            }
          case zFailure(error) =>
            currentSender ! error
        }
    }
  }

}