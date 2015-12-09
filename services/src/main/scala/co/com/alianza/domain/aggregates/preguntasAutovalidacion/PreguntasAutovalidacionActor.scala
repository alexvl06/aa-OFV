package co.com.alianza.domain.aggregates.preguntasAutovalidacion


import akka.actor.{ActorRef, Props, ActorLogging, Actor}
import akka.routing.RoundRobinPool
import co.com.alianza.app.{AlianzaActors, MainActors}
import co.com.alianza.commons.enumerations.TiposCliente
import co.com.alianza.exceptions.PersistenceException
import co.com.alianza.infrastructure.anticorruption.preguntasAutovalidacion.DataAccessAdapter
import co.com.alianza.infrastructure.dto.{RespuestaCompleta, Respuesta, Pregunta}
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
import scalaz.{Failure => zFailure, Success => zSuccess, Validation}


class PreguntasAutovalidacionSupervisor extends Actor with ActorLogging {
  import akka.actor.OneForOneStrategy
  import akka.actor.SupervisorStrategy._

  val preguntasAutovalidacionActor = context.actorOf(Props[PreguntasAutovalidacionActor].withRouter(RoundRobinPool(nrOfInstances = 2)), "preguntasAutovalidacionActor")

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


class PreguntasAutovalidacionActor extends Actor with ActorLogging with AlianzaActors with FutureResponse  {
  import scala.concurrent.ExecutionContext
  implicit val _: ExecutionContext = context.dispatcher
  private val config: Config = MainActors.conf

  private[this] val preguntasAutovalidacionRepository = new PreguntasAutovalidacionRepository()

  def receive = {
    case message:ObtenerPreguntasMessage =>
      val currentSender = sender()
      val future: Future[Validation[PersistenceException, List[Pregunta]]] = DataAccessAdapter.obtenerPreguntas()
      resolveFutureValidation(future, (response: List[Pregunta]) => JsonUtil.toJson(response), currentSender)

    case message:GuardarRespuestasMessage =>
      val currentSender = sender()
      message.tipoCliente match {
        case Some("clienteIndividual") => guardarRespuestasUsuario(message.idUsuario, message.respuestas, currentSender)
        case Some("clienteAdministrador") => guardarRespuestasClienteAdministrador(message.idUsuario, message.respuestas, currentSender)
      }

    case message:ObtenerPreguntasRandomMessage =>
      val currentSender = sender()
      message.tipoCliente match {
        case Some("clienteIndividual") => obtenerPreguntasRandomClienteIndividual(message.idUsuario, currentSender)
      }

    case message:ValidarRespuestasMessage =>
      val currentSender = sender()
      message.tipoCliente match {
        case Some("clienteIndividual") => validarRespuestasClienteIndividual(message.idUsuario, message.respuestas, currentSender)
      }
  }

  /**
   * Guardar respuestas de autovalidacion para usuario individual
   * @param idUsuario
   * @param respuestas
   * @param currentSender
   */
  def guardarRespuestasUsuario(idUsuario: Option[Int], respuestas: List[Respuesta], currentSender: ActorRef): Unit = {
    val respuestasPersistencia = respuestas.map( x => new RespuestasAutovalidacionUsuario(x.idPregunta, idUsuario.get, x.respuesta))
    val futuro = DataAccessAdapter.guardarRespuestas(respuestasPersistencia)
    futuro  onComplete {
      case Failure(failure) => currentSender ! failure
      case Success(value)   =>
        value match {
          case zSuccess(response: List[Int]) =>
            currentSender !  ResponseMessage(OK)
          case zFailure(error) =>  currentSender !  error
        }
    }
  }

  /**
   * Guardar respuestas de autovalidacion para Cliente Administrador
   * @param idUsuario
   * @param respuestas
   * @param currentSender
   */
  def guardarRespuestasClienteAdministrador(idUsuario: Option[Int], respuestas: List[Respuesta], currentSender: ActorRef): Unit = {
    val respuestasPersistencia = respuestas.map( x => new RespuestasAutovalidacionUsuario(x.idPregunta, idUsuario.get, x.respuesta))
    val futuro = DataAccessAdapter.guardarRespuestasClienteAdministrador(respuestasPersistencia)
    futuro  onComplete {
      case Failure(failure) => currentSender ! failure
      case Success(value)   =>
        value match {
          case zSuccess(response: List[Int]) =>
            currentSender !  ResponseMessage(OK)
          case zFailure(error) =>  currentSender !  error
        }
    }
  }

  /**
   * Obtener 3 preguntas al azar del cliente individual
   * @param idUsuario
   * @param currentSender
   */
  def obtenerPreguntasRandomClienteIndividual(idUsuario: Option[Int], currentSender: ActorRef): Unit = {
    val futuro = DataAccessAdapter.obtenerPreguntasClienteIndividual(idUsuario)
    futuro  onComplete {
      case Failure(failure) => currentSender ! failure
      case Success(value)   =>
        value match {
          case zSuccess(response: List[Pregunta]) => {
            val respuestaRandom = Random.shuffle(response).take(3)
            currentSender !  ResponseMessage(OK, JsonUtil.toJson( respuestaRandom ))
          }
          case zFailure(error) =>  currentSender !  error
        }
    }
  }

  def validarRespuestasClienteIndividual(idUsuario: Option[Int], respuestas: List[Respuesta], currentSender: ActorRef): Unit = {
    val futuro = DataAccessAdapter.obtenerRespuestaCompletaClienteIndividual(idUsuario)
    futuro  onComplete {
      case Failure(failure) => currentSender ! failure
      case Success(value)   =>
        value match {
          case zSuccess(response: List[RespuestaCompleta]) =>
            val respuestasGuardadas : List[Respuesta] = response.map(res => Respuesta(res.idPregunta, res.respuesta))
            //comprobar que las respuestas concuerden
            val existe :Boolean = respuestas.foldLeft(true)((existe,respuesta) => {
              existe && respuestasGuardadas.contains(respuesta)
            })
            existe match {
              case true => currentSender !  ResponseMessage(OK)
              case false =>{
                //en caso que no concuerden, se envian la preguntas restantes mas una de las contestadas
                val idsRespuesta : List[Int] = respuestas.map(_.idPregunta)
                val idsPreguntas : List[Int] = response.filter(res => !idsRespuesta.contains(res.idPregunta)).map(_.idPregunta) ++ Random.shuffle(idsRespuesta).take(1)
                val preguntas : List[Pregunta] = response.filter(res => idsPreguntas.contains(res.idPregunta)).map(x=>Pregunta(x.idPregunta, x.pregunta))
                currentSender !  ResponseMessage(Conflict, JsonUtil.toJson(Random.shuffle(preguntas).take(3)))
              }
            }
          case zFailure(error) =>  currentSender !  error
        }
    }
  }
}
