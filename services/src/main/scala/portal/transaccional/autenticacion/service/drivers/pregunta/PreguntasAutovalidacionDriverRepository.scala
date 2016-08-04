package portal.transaccional.autenticacion.service.drivers.pregunta

import akka.util.Timeout
import co.com.alianza.commons.enumerations.TiposCliente
import co.com.alianza.commons.enumerations.TiposCliente.TiposCliente
import co.com.alianza.exceptions.ValidacionException
import co.com.alianza.infrastructure.dto.{ Configuracion, Pregunta }
import co.com.alianza.infrastructure.messages.PreguntasComprobarResponse
import co.com.alianza.persistence.entities.{ Configuraciones, PreguntaAutovalidacion }
import enumerations.ConfiguracionEnum
import portal.transaccional.autenticacion.service.drivers.configuracion.{ ConfiguracionRepository, DataAccessTranslator => configuracionDTO }
import portal.transaccional.autenticacion.service.drivers.pregunta.{ DataAccessTranslator => preguntasAutovalidacionDTO }
import portal.transaccional.autenticacion.service.web.preguntasAutovalidacion.{ ResponseObtenerPreguntas, ResponseObtenerPreguntasComprobar }
import portal.transaccional.fiduciaria.autenticacion.storage.daos.portal.AlianzaDAOs

import scala.concurrent.duration._
import scala.concurrent.{ ExecutionContext, Future }
import scala.util._

case class PreguntasAutovalidacionDriverRepository(
  preguntasRepository: PreguntasRepository,
    configuracionRepository: ConfiguracionRepository, alianzaDao: AlianzaDAOs
)(implicit val ex: ExecutionContext) extends PreguntasAutovalidacionRepository {

  implicit val timeout = Timeout(5.seconds)

  /**
   * Obtener las preguntas disponibles
   * El numero de preguntas que se envia, es igual a el numero de preguntas
   * que debe aparecer en la lista desplegable.
   */
  def obtenerPreguntas(): Future[ResponseObtenerPreguntas] = {
    for {
      preguntas <- preguntasRepository.obtenerPreguntas()
      configuraciones <- configuracionRepository.getAll()
      respuesta <- resolveObtenerPreguntas(preguntas.toList, configuraciones.toList)
    } yield respuesta
  }

  private def resolveObtenerPreguntas(preguntasEntities: List[PreguntaAutovalidacion], configuracionesEntities: List[Configuraciones]): Future[ResponseObtenerPreguntas] = Future {
    val preguntasDto = preguntasEntities.map(pregunta => preguntasAutovalidacionDTO.entityToDto(pregunta))
    val configuracionesDto = configuracionesEntities.map(conf => configuracionDTO.entityToDto(conf))
    val numeroPreguntas = obtenerValorEntero(configuracionesDto, ConfiguracionEnum.AUTOVALIDACION_NUMERO_PREGUNTAS.name)
    val numeroPreguntasLista = obtenerValorEntero(configuracionesDto, ConfiguracionEnum.AUTOVALIDACION_NUMERO_PREGUNTAS_LISTA.name)
    val preguntas = Random.shuffle(preguntasDto).take(numeroPreguntasLista)
    ResponseObtenerPreguntas(preguntasDto, numeroPreguntas)
  }

  private def obtenerValorEntero(configuraciones: List[Configuracion], llave: String): Int = {
    configuraciones.filter(conf => conf.llave.equals(llave)).head.valor.toInt
  }

  /**
   * Obtener preguntas al azar del cliente
   * de acuerdo a las parametrizaciones
   */
  def obtenerPreguntasComprobar(idUsuario: Int, tipoCliente: TiposCliente): Future[ResponseObtenerPreguntasComprobar] = {
    val llaveNumeroPreguntas: String = ConfiguracionEnum.AUTOVALIDACION_NUMERO_PREGUNTAS.name
    val futurePreguntas = tipoCliente match {
      case TiposCliente.clienteIndividual => alianzaDao.getIndividualClientQuestions(idUsuario)
      // TODO: Cliente Administrador
      case _ => Future(List.empty[Pregunta])
    }

    for {
      preguntas <- alianzaDao.getIndividualClientQuestions(idUsuario)
      configuraciones <- configuracionRepository.getAll()
      validar <- validarParametrizacion(preguntas.size, configuraciones.toList, llaveNumeroPreguntas)
      respuesta <- resolveObtenerPreguntasComprobar(preguntas.toList, configuraciones.toList)
    } yield respuesta

  }

  private def resolveObtenerPreguntasComprobar(preguntasEntities: List[PreguntaAutovalidacion], configuracionesEntities: List[Configuraciones]): Future[ResponseObtenerPreguntasComprobar] = Future {
    val preguntasDto = preguntasEntities.map(pregunta => preguntasAutovalidacionDTO.entityToDto(pregunta))
    val configuracionesDto = configuracionesEntities.map(conf => configuracionDTO.entityToDto(conf))
    val numeroIntentos = obtenerValorEntero(configuracionesDto, ConfiguracionEnum.AUTOVALIDACION_NUMERO_REINTENTOS.name)
    val numeroPreguntasComprobacion = obtenerValorEntero(configuracionesDto, ConfiguracionEnum.AUTOVALIDACION_NUMERO_PREGUNTAS_COMPROBACION.name)
    val preguntasRandom = Random.shuffle(preguntasDto).take(numeroPreguntasComprobacion)
    ResponseObtenerPreguntasComprobar(preguntasRandom, numeroIntentos)
  }

  private def validarParametrizacion(numeroRespuestas: Int, configuraciones: List[Configuraciones], llaveNumeroPreguntas: String): Future[Boolean] = {
    val configuracionesDto = configuraciones.map(conf => configuracionDTO.entityToDto(conf))
    val numeroRespuestasParametrizadas = obtenerValorEntero(configuracionesDto, llaveNumeroPreguntas)
    val comparacion = numeroRespuestas == numeroRespuestasParametrizadas
    comparacion match {
      case true => Future.successful(comparacion)
      case false => Future.failed(ValidacionException("", "Error comprobacion parametrizacion campos"))
    }
  }
}
