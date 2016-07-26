package co.com.alianza.domain.aggregates.confronta

import java.sql.Timestamp

import akka.actor.{ Actor, ActorLogging, ActorRef, ActorSystem, Props }

import scalaz.{ Failure => zFailure, Success => zSuccess }
import scalaz.std.AllInstances._
import scala.util.{ Failure, Success }
import co.com.alianza.app.{ AlianzaActors }
import com.typesafe.config.{ Config }
import co.cifin.confrontaultra.dto.ultra._
import com.asobancaria.cifinpruebas.cifin.confrontav2plusws.services.ConfrontaUltraWS.{ ConfrontaUltraWSSoapBindingStub, ConfrontaUltraWebServiceServiceLocator }
import co.cifin.confrontaultra.dto.ultras.RespuestaCuestionarioULTRADTO
import co.com.alianza.infrastructure.anticorruption.usuarios.{ DataAccessAdapter => DataAccessAdapterUsuario }
import co.com.alianza.infrastructure.anticorruption.ultimasContrasenas.{ DataAccessAdapter => DataAccessAdapterUltimaContrasena }
import co.com.alianza.util.clave.Crypto
import enumerations.{ AppendPasswordUser, PerfilesUsuario }
import co.com.alianza.domain.aggregates.usuarios.ErrorPersistence
import akka.routing.RoundRobinPool
import co.com.alianza.persistence.entities.PerfilUsuario
import co.com.alianza.infrastructure.messages.{ ObtenerCuestionarioAdicionalRequestMessage, UsuarioMessage, ValidarCuestionarioDesbloqueoRequestMessage, ValidarCuestionarioRequestMessage }
import co.com.alianza.persistence.entities.UltimaContrasena
import co.com.alianza.util.transformers.ValidationT

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

class ConfrontaActor(implicit val system: ActorSystem) extends Actor with ActorLogging with AlianzaActors {

  import system.dispatcher
  implicit val config: Config = system.settings.config
  import co.com.alianza.util.json.MarshallableImplicits._


  def receive = {

    case message: ObtenerCuestionarioAdicionalRequestMessage =>
      val currentSender = sender()

    case message: ValidarCuestionarioDesbloqueoRequestMessage =>
      val currentSender = sender()
      val respuestas = new java.util.ArrayList[RespuestaPreguntaULTRADTO]()
      for (res <- message.respuestas) {
        val split = res.split(",")
        respuestas.add(new RespuestaPreguntaULTRADTO(split(0).toInt, split(1).toInt))
      }

      val locator: ConfrontaUltraWebServiceServiceLocator = new ConfrontaUltraWebServiceServiceLocator(config.getString("confronta.service.obtenerCuestionario.location"))
      val stub: ConfrontaUltraWSSoapBindingStub = locator.getConfrontaUltraWS.asInstanceOf[ConfrontaUltraWSSoapBindingStub]
      val parametros: ParametrosSeguridadULTRADTO = new ParametrosSeguridadULTRADTO
      parametros.setClaveCIFIN(config.getString("confronta.service.claveCIFIN"))
      parametros.setPassword(config.getString("confronta.service.password"))
      val parametrosUltra: RespuestaCuestionarioULTRADTO = new RespuestaCuestionarioULTRADTO(message.secuenciaCuestionario, message.codigoCuestionario, respuestas.toArray(new Array[RespuestaPreguntaULTRADTO](respuestas.size())))
      val response = stub.evaluarCuestionario(parametros, parametrosUltra)

      if (response.getRespuestaProceso.getCodigoRespuesta == 1) {
        actualizarEstadoDesbloqueo(message.id, response, currentSender)
      } else {
        val respToSender = new ResultadoEvaluacionCuestionarioULTRADTO()
        respToSender.setRespuestaProceso(response.getRespuestaProceso)
        respToSender.getRespuestaProceso.setDescripcionRespuesta("No es posible realizar el registro, por favor llamar a la línea de atención. ")
        currentSender ! respToSender.toJson
      }

    case message: ValidarCuestionarioRequestMessage =>
      val currentSender = sender()
      val respuestas = new java.util.ArrayList[RespuestaPreguntaULTRADTO]()
      for (res <- message.respuestas) {
        val split = res.split(",")
        respuestas.add(new RespuestaPreguntaULTRADTO(split(0).toInt, split(1).toInt))
      }

      val locator: ConfrontaUltraWebServiceServiceLocator = new ConfrontaUltraWebServiceServiceLocator(config.getString("confronta.service.obtenerCuestionario.location"))
      val stub: ConfrontaUltraWSSoapBindingStub = locator.getConfrontaUltraWS.asInstanceOf[ConfrontaUltraWSSoapBindingStub]
      val parametros: ParametrosSeguridadULTRADTO = new ParametrosSeguridadULTRADTO
      parametros.setClaveCIFIN(config.getString("confronta.service.claveCIFIN"))
      parametros.setPassword(config.getString("confronta.service.password"))
      val parametrosUltra: RespuestaCuestionarioULTRADTO = new RespuestaCuestionarioULTRADTO(message.secuenciaCuestionario, message.codigoCuestionario, respuestas.toArray(new Array[RespuestaPreguntaULTRADTO](respuestas.size())))
      val response = stub.evaluarCuestionario(parametros, parametrosUltra)

      if (response.getRespuestaProceso.getCodigoRespuesta == 1) {
        actualizarEstadoConfronta(message.id, response, currentSender)
      } else {
        val respToSender = new ResultadoEvaluacionCuestionarioULTRADTO()
        respToSender.setRespuestaProceso(response.getRespuestaProceso)
        respToSender.getRespuestaProceso.setDescripcionRespuesta("No es posible realizar el registro, por favor llamar a la línea de atención. ")
        currentSender ! respToSender.toJson
      }

  }

  private def actualizarEstadoConfronta(message: UsuarioMessage, response: ResultadoEvaluacionCuestionarioULTRADTO, currentSender: ActorRef) = {
    val passwordUserWithAppend = message.contrasena.concat(AppendPasswordUser.appendUsuariosFiducia)
    val UsuarioCreadoFuture = (for {
      resultActualizarEstadoConfronta <- ValidationT(DataAccessAdapterUsuario.crearUsuario(message.toEntityUsuario).map(_.leftMap(pe => ErrorPersistence(pe.message, pe))))
      usuario <- ValidationT(DataAccessAdapterUsuario.obtenerUsuarioNumeroIdentificacion(message.identificacion).map(_.leftMap(pe => ErrorPersistence(pe.message, pe))))
      cambioContrasena <- ValidationT(DataAccessAdapterUsuario.cambiarPassword(usuario.get.id.get, Crypto.hashSha512(passwordUserWithAppend, usuario.get.id.get)).map(_.leftMap(pe => ErrorPersistence(pe.message, pe))))
      resultGuardarUltimasContrasenas <- ValidationT(DataAccessAdapterUltimaContrasena.guardarUltimaContrasena(UltimaContrasena(None, resultActualizarEstadoConfronta, Crypto.hashSha512(passwordUserWithAppend, usuario.get.id.get), new Timestamp(System.currentTimeMillis()))).map(_.leftMap(pe => ErrorPersistence(pe.message, pe))))
    } yield {
      resultActualizarEstadoConfronta
    }).run

    UsuarioCreadoFuture onComplete {
      case Failure(failure) => currentSender ! failure
      case Success(value) =>
        value match {
          case zSuccess(code: Int) =>
            val respToSender = new ResultadoEvaluacionCuestionarioULTRADTO()
            respToSender.setRespuestaProceso(response.getRespuestaProceso)
            DataAccessAdapterUsuario.asociarPerfiles(PerfilUsuario(code, PerfilesUsuario.clienteIndividual.id) :: Nil)
            if (message.activarIP && message.clientIp.isDefined) {
              for { a <- DataAccessAdapterUsuario.relacionarIp(code, message.clientIp.get) } yield a
            }
            currentSender ! respToSender.toJson
          case zFailure(error) =>
            currentSender ! error
        }
    }
  }

  private def actualizarEstadoDesbloqueo(id: String, response: ResultadoEvaluacionCuestionarioULTRADTO, currentSender: ActorRef) = {

    val resultActualizarEstadoConfronta = DataAccessAdapterUsuario.actualizarEstadoConfronta(id)
    resultActualizarEstadoConfronta onComplete {
      case Failure(failure) => currentSender ! failure
      case Success(value) =>
        value match {
          case zSuccess(code: Int) =>
            val respToSender = new ResultadoEvaluacionCuestionarioULTRADTO()
            respToSender.setRespuestaProceso(response.getRespuestaProceso)
            currentSender ! respToSender.toJson
          case zFailure(error) =>
            currentSender ! error
        }
    }
  }

}
