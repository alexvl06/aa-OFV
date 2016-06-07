package co.com.alianza.domain.aggregates.autovalidacion

import akka.actor.{ Props, ActorLogging, Actor }
import akka.routing.RoundRobinPool
import co.com.alianza.app.{ AlianzaActors, MainActors }
import co.com.alianza.commons.enumerations.TiposCliente
import co.com.alianza.domain.aggregates.usuarios.{ ErrorValidacion, ErrorAutovalidacion }
import co.com.alianza.exceptions.PersistenceException
import co.com.alianza.infrastructure.anticorruption.autovalidacion.DataAccessAdapter
import co.com.alianza.infrastructure.anticorruption.configuraciones.{ DataAccessAdapter => DataAdapterConfiguracion }

import co.com.alianza.infrastructure.dto.{ Configuracion, RespuestaCompleta, Respuesta, Pregunta }
import co.com.alianza.infrastructure.messages._
import co.com.alianza.persistence.entities.RespuestasAutovalidacionUsuario
import co.com.alianza.util.FutureResponse
import co.com.alianza.util.json.JsonUtil
import co.com.alianza.util.transformers.ValidationT
import com.typesafe.config.Config
import spray.http.StatusCodes._

import scala.concurrent.Future
import scala.util._
import scalaz.{ Success => zSuccess, Failure => zFailure, Validation }

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
  import scalaz.std.AllInstances._
  import co.com.alianza.domain.aggregates.usuarios.ValidacionesUsuario.errorValidacion
  import co.com.alianza.domain.aggregates.usuarios.ValidacionesUsuario.toErrorValidation
  implicit val _: ExecutionContext = context.dispatcher

  private val config: Config = MainActors.conf

  def receive = {

    case message: ObtenerPreguntasMessage => obtenerPreguntas()

    case message: GuardarRespuestasMessage => guardarRespuestas(message)

    case message: ObtenerPreguntasComprobarMessage => obtenerPreguntasComprobar(message)

    case message: ValidarRespuestasMessage => validarRespuestas(message)

    case message: BloquearRespuestasMessage => bloquearRespuestas(message)

  }

  /**
   * Obtener las preguntas disponibles
   * El numero de preguntas que se envia, es igual a el numero de preguntas
   * que debe aparecer en la lista desplegable.
   */
  private def obtenerPreguntas(): Unit = {
    val currentSender = sender()
    val future: Future[Validation[PersistenceException, (List[Pregunta], List[Configuracion])]] = (for {
      preguntas <- ValidationT(DataAccessAdapter.obtenerPreguntas())
      configuraciones <- ValidationT(DataAdapterConfiguracion.obtenerConfiguraciones())
    } yield (preguntas, configuraciones)).run
    resolveFutureValidation(future, (response: (List[Pregunta], List[Configuracion])) => {
      val numeroPreguntas = obtenerValorEntero(response._2, "AUTOVALIDACION_NUMERO_PREGUNTAS")
      val numeroPreguntasLista = obtenerValorEntero(response._2, "AUTOVALIDACION_NUMERO_PREGUNTAS_LISTA")
      val preguntas = Random.shuffle(response._1).take(numeroPreguntasLista)
      JsonUtil.toJson(PreguntasResponse(preguntas, numeroPreguntas))
    }, errorValidacion, currentSender)
  }

  /**
   * Guardar respuestas autovalidacion
   * @param message
   */
  private def guardarRespuestas(message: GuardarRespuestasMessage) = {
    val currentSender = sender()
    val respuestasPersistencia = message.respuestas.map(x => new RespuestasAutovalidacionUsuario(x.idPregunta, message.idUsuario, x.respuesta))
    //futuro del guardar
    val futureGuardar: Future[Validation[PersistenceException, List[Int]]] = message.tipoCliente match {
      case TiposCliente.clienteIndividual => DataAccessAdapter.guardarRespuestasClienteIndividual(respuestasPersistencia)
      case TiposCliente.clienteAdministrador => DataAccessAdapter.guardarRespuestasClienteAdministrador(respuestasPersistencia)
      case _ => Future(zSuccess(List.empty[Int]))
    }
    val future: Future[Validation[ErrorValidacion, List[Int]]] = (for {
      configuracion <- ValidationT(toErrorValidation(DataAdapterConfiguracion.obtenerConfiguracionPorLlave("AUTOVALIDACION_NUMERO_PREGUNTAS")))
      validar <- ValidationT(validarNumeroRespuestas(respuestasPersistencia.size, configuracion.get.valor.toInt))
      guardar <- ValidationT(toErrorValidation(futureGuardar))
    } yield guardar).run
    resolveFutureValidation(future, (response: List[Int]) => ResponseMessage(OK), errorValidacion, currentSender)
  }

  /**
   * Validaciones correspondiente a que el numero de respuestas sea igual a
   * el numero de preguntas parametrizadas por el administrados
   * @param numeroRespuestas
   * @param numeroRespuestasParametrizadas
   * @return
   */
  private def validarNumeroRespuestas(numeroRespuestas: Int, numeroRespuestasParametrizadas: Int): Future[Validation[ErrorValidacion, Boolean]] = Future {
    val comparacion = numeroRespuestas == numeroRespuestasParametrizadas
    comparacion match {
      case true => zSuccess(comparacion)
      case false =>
        //TODO: definir los codigos de error a enviar para este error y el
        //comportamiento
        zFailure(ErrorAutovalidacion("Numero de respuestas no es igual al parametrizado"))
    }
  }

  /**
   * Obtener preguntas al azar del cliente individual
   * de acuerdo a las parametrizaciones
   */
  private def obtenerPreguntasComprobar(message: ObtenerPreguntasComprobarMessage) = {
    val currentSender = sender()
    val futurePreguntas: Future[Validation[PersistenceException, List[Pregunta]]] = message.tipoCliente match {
      case TiposCliente.clienteIndividual => DataAccessAdapter.obtenerPreguntasClienteIndividual(message.idUsuario)
      case TiposCliente.clienteAdministrador => DataAccessAdapter.obtenerPreguntasClienteAdministrador(message.idUsuario)
      case _ => Future(zSuccess(List.empty[Pregunta]))
    }
    val future = (for {
      preguntas <- ValidationT(futurePreguntas)
      configuracion <- ValidationT(DataAdapterConfiguracion.obtenerConfiguracionPorLlave("AUTOVALIDACION_NUMERO_PREGUNTAS_COMPROBACION"))
    } yield (preguntas, configuracion)).run
    resolveFutureValidation(future, (response: (List[Pregunta], Option[Configuracion])) => {
      val respuestaRandom = Random.shuffle(response._1).take(response._2.get.valor.toInt)
      JsonUtil.toJson(respuestaRandom)
    }, errorValidacion, currentSender)
  }

  /**
   * Validar las respuestas
   * @param message
   */
  private def validarRespuestas(message: ValidarRespuestasMessage): Unit = {
    val currentSender = sender()
    val futureRespuestas: Future[Validation[PersistenceException, List[RespuestaCompleta]]] = message.tipoCliente match {
      case TiposCliente.clienteIndividual => DataAccessAdapter.obtenerRespuestaCompletaClienteIndividual(message.idUsuario)
      case TiposCliente.clienteAdministrador => DataAccessAdapter.obtenerRespuestaCompletaClienteAdministrador(message.idUsuario)
      case _ => Future(zSuccess(List.empty[RespuestaCompleta]))
    }
    val llavePreguntasComprobar: String = "AUTOVALIDACION_NUMERO_PREGUNTAS_COMPROBACION"
    val llavePreguntasCambio: String = "AUTOVALIDACION_NUMERO_PREGUNTAS_CAMBIAR"
    val future = (for {
      configuraciones <- ValidationT(toErrorValidation(DataAdapterConfiguracion.obtenerConfiguraciones()))
      validar <- ValidationT(validarNumeroRespuestas(message.respuestas.size, obtenerValorEntero(configuraciones, llavePreguntasComprobar)))
      respuestas <- ValidationT(toErrorValidation(futureRespuestas))
      respuesta <- ValidationT(validarRespuestasValidation(respuestas, message.respuestas, obtenerValorEntero(configuraciones, llavePreguntasCambio)))
    } yield respuesta).run
    resolveFutureValidation(future, (response: String) => response, errorValidacion, currentSender)
  }

  private def obtenerValorEntero(configuraciones: List[Configuracion], llave: String): Int = {
    configuraciones.filter(conf => conf.llave.equals(llave)).head.valor.toInt
  }

  /**
   * Validar respuestas y responder si no concuerdan
   * @param response
   * @param respuestas
   * @return
   */
  private def validarRespuestasValidation(response: List[RespuestaCompleta], respuestas: List[Respuesta], numeroPreguntasCambio: Int): Future[Validation[ErrorValidacion, String]] = Future {
    val respuestasGuardadas: List[Respuesta] = response.map(res => Respuesta(res.idPregunta, res.respuesta))
    //comprobar que las respuestas concuerden
    val existe: Boolean = respuestas.foldLeft(true)((existe, respuesta) => existe && respuestasGuardadas.contains(respuesta))
    existe match {
      case true => zSuccess("OK")
      case false => {
        //en caso que no concuerden, se envian la preguntas restantes mas una de las contestadas
        //1. obtener los ids de las respuestas
        val idsRespuesta: List[Int] = respuestas.map(_.idPregunta)
        //2. obtener los ids de las preguntas que se van a repetir
        val numeroPreguntasRepetidas: Int = respuestas.size - numeroPreguntasCambio
        val idsPreguntasRepetidas: List[Int] = Random.shuffle(idsRespuesta).take(numeroPreguntasRepetidas)
        //3. obtener ids de las preguntas que no corresponden a las preguntas contestadas
        val idsPreguntasNuevas: List[Int] = response.filter(res => !idsRespuesta.contains(res.idPregunta)).map(_.idPregunta)
        //4. obtener ids de las preguntas repetidas mas las preguntas nuevas
        val idsPreguntas: List[Int] = idsPreguntasRepetidas ++ Random.shuffle(idsPreguntasNuevas).take(numeroPreguntasCambio)
        //5. con los ids, obtener las preguntas a devolver
        val preguntas: List[Pregunta] = response.filter(res => (idsPreguntas).contains(res.idPregunta)).map(x => Pregunta(x.idPregunta, x.pregunta))
        //6. reenviar preguntas desordenadamente
        zFailure(ErrorAutovalidacion(JsonUtil.toJson(Random.shuffle(preguntas).take(preguntas.size))))
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
