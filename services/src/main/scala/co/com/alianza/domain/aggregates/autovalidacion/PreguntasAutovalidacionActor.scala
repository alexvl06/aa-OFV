package co.com.alianza.domain.aggregates.autovalidacion

import akka.actor.{ ActorRef, Props, ActorLogging, Actor }
import akka.routing.RoundRobinPool
import co.com.alianza.app.{ AlianzaActors, MainActors }
import co.com.alianza.commons.enumerations.TiposCliente
import co.com.alianza.exceptions.PersistenceException
import co.com.alianza.infrastructure.anticorruption.preguntasAutovalidacion.DataAccessAdapter
import co.com.alianza.infrastructure.dto.{ RespuestaCompleta, Respuesta, Pregunta }
import co.com.alianza.infrastructure.messages._
import co.com.alianza.persistence.entities.RespuestasAutovalidacionUsuario
import co.com.alianza.persistence.repositories.PreguntasAutovalidacionRepository
import co.com.alianza.util.FutureResponse
import co.com.alianza.util.json.JsonUtil
import com.typesafe.config.Config
import spray.http.StatusCodes._

import scala.concurrent.Future
import scala.util.Success
import scala.util._
import scalaz.{ Failure => zFailure, Success => zSuccess, Validation }

class PreguntasAutovalidacionSupervisor extends Actor with ActorLogging {
  import akka.actor.OneForOneStrategy
  import akka.actor.SupervisorStrategy._

  val preguntasAutovalidacionActor = context.actorOf(Props[PreguntasAutovalidacionActor]
    .withRouter(RoundRobinPool(nrOfInstances = 2)), "preguntasAutovalidacionActor")

  def receive = {
    case message: Any =>
      preguntasAutovalidacionActor forward message
  }

  override val supervisorStrategy = OneForOneStrategy() {
    case exception: Exception =>
      exception.printStackTrace()
      log.error(exception, exception.getMessage)
      Restart
  }
}

class PreguntasAutovalidacionActor extends Actor with ActorLogging with AlianzaActors with FutureResponse {
  import scala.concurrent.ExecutionContext
  import co.com.alianza.domain.aggregates.usuarios.ValidacionesUsuario.errorValidacion
  implicit val _: ExecutionContext = context.dispatcher
  private val config: Config = MainActors.conf

  def receive = {

    case message: ObtenerPreguntasMessage => obtenerPreguntas()

    case message: GuardarRespuestasMessage => guardarRespuestas(message)

    case message: ObtenerPreguntasRandomMessage => obtenerPreguntasRandom(message)

    case message: ValidarRespuestasMessage => validarRespuestas(message)

    case message: BloquearRespuestasMessage => bloquearRespuestas(message)

  }

  /**
   * Obtener las preguntas disponibles
   */
  private def obtenerPreguntas(): Unit = {
    //TODO: Parametrizar el numero de preguntar (#responder * #lista)
    val currentSender = sender()
    val future: Future[Validation[PersistenceException, List[Pregunta]]] = DataAccessAdapter.obtenerPreguntas()
    resolveFutureValidation(future, (response: List[Pregunta]) => JsonUtil.toJson(response), errorValidacion, currentSender)
  }

  /**
   * Guardar respuestas autovalidacion
   * @param message
   */
  private def guardarRespuestas(message: GuardarRespuestasMessage) = {
    //TODO: validar que el #respuestas = #respuestasParametrizadas
    val currentSender = sender()
    val respuestasPersistencia = message.respuestas.map(x => new RespuestasAutovalidacionUsuario(x.idPregunta, message.idUsuario, x.respuesta))
    //futuro del guardar
    val future: Future[Validation[PersistenceException, List[Int]]] = message.tipoCliente match {
      case TiposCliente.clienteIndividual => DataAccessAdapter.guardarRespuestasClienteIndividual(respuestasPersistencia)
      case TiposCliente.clienteAdministrador => DataAccessAdapter.guardarRespuestasClienteAdministrador(respuestasPersistencia)
      case _ => Future(zSuccess(List.empty[Int]))
    }
    resolveFutureValidation(future, (response: List[Int]) => ResponseMessage(OK), errorValidacion, currentSender)
  }

  /**
   * Obtener preguntas al azar del cliente individual
   * de acuerdo a las parametrizaciones
   */
  private def obtenerPreguntasRandom(message: ObtenerPreguntasRandomMessage) = {
    //TODO: parametrizar el #preguntasAResponder. Que pasa si ahora son mas ???
    val currentSender = sender()
    val future: Future[Validation[PersistenceException, List[Pregunta]]] = message.tipoCliente match {
      case TiposCliente.clienteIndividual => DataAccessAdapter.obtenerPreguntasClienteIndividual(message.idUsuario)
      case TiposCliente.clienteAdministrador => DataAccessAdapter.obtenerPreguntasClienteAdministrador(message.idUsuario)
      case _ => Future(zSuccess(List.empty[Pregunta]))
    }
    resolveFutureValidation(future, obtenerRespuestasAleatorias, errorValidacion, currentSender)
  }

  /**
   * Obtener respuestas aleatorias de acuerdo
   * a la parametrizacion
   * @param response
   * @return
   */
  private def obtenerRespuestasAleatorias(response: List[Pregunta]): String = {
    //TODO: Aqui debe ir la parametrizacion
    val numeroPreguntas = 3
    val respuestaRandom = Random.shuffle(response).take(numeroPreguntas)
    JsonUtil.toJson(respuestaRandom)
  }

  /**
   * Validar las respuestas
   * @param message
   */
  private def validarRespuestas(message: ValidarRespuestasMessage): Unit = {
    val currentSender = sender()
    val future: Future[Validation[PersistenceException, List[RespuestaCompleta]]] = message.tipoCliente match {
      case TiposCliente.clienteIndividual => DataAccessAdapter.obtenerRespuestaCompletaClienteIndividual(message.idUsuario)
      case TiposCliente.clienteAdministrador => DataAccessAdapter.obtenerRespuestaCompletaClienteAdministrador(message.idUsuario)
      case _ => Future(zSuccess(List.empty[RespuestaCompleta]))
    }
    resolveFutureValidation(future, (response: List[RespuestaCompleta]) => validarRespuestas(response, message.respuestas), errorValidacion, currentSender)
  }

  /**
   * Validar respuestas y responder si no concuerdan
   * @param response
   * @param respuestas
   * @return
   */
  private def validarRespuestas(response: List[RespuestaCompleta], respuestas: List[Respuesta]): String = {
    //TODO: validar la parametrizacion, para que #respuestas = #respuestasParametrizadas
    val respuestasGuardadas: List[Respuesta] = response.map(res => Respuesta(res.idPregunta, res.respuesta))
    //comprobar que las respuestas concuerden
    val existe: Boolean = respuestas.foldLeft(true)((existe, respuesta) => existe && respuestasGuardadas.contains(respuesta))
    existe match {
      case true => JsonUtil.toJson("OK")
      case false => {
        //en caso que no concuerden, se envian la preguntas restantes mas una de las contestadas
        val idsRespuesta: List[Int] = respuestas.map(_.idPregunta)
        val idsPreguntas: List[Int] = response.filter(res => !idsRespuesta.contains(res.idPregunta)).map(_.idPregunta) ++ Random.shuffle(idsRespuesta).take(1)
        val preguntas: List[Pregunta] = response.filter(res => idsPreguntas.contains(res.idPregunta)).map(x => Pregunta(x.idPregunta, x.pregunta))
        //reenviar preguntas
        obtenerRespuestasAleatorias(preguntas)
      }
    }
  }

  /**
   * Bloquear respuestas autovalidacion
   * @param message
   */
  def bloquearRespuestas(message: BloquearRespuestasMessage) = {
    val currentSender = sender()
    val future: Future[Validation[PersistenceException, Int]] = message.tipoCliente match {
      case TiposCliente.clienteIndividual => DataAccessAdapter.bloquearRespuestasClienteIndividual(message.idUsuario)
      case TiposCliente.clienteAdministrador => DataAccessAdapter.bloquearRespuestasClienteAdministrador(message.idUsuario)
      case _ => Future(zSuccess(Integer.MIN_VALUE))
    }
    resolveFutureValidation(future, (response: Int) => ResponseMessage(OK), errorValidacion, currentSender)
  }

}
