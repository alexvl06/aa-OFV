package portal.transaccional.autenticacion.service.drivers.pregunta

import akka.util.Timeout
import co.com.alianza.commons.enumerations.TiposCliente
import co.com.alianza.commons.enumerations.TiposCliente.TiposCliente
import co.com.alianza.exceptions.ValidacionException
import co.com.alianza.infrastructure.dto.security.UsuarioAuth
import co.com.alianza.infrastructure.dto.{ Pregunta, Respuesta, RespuestaCompleta }
import co.com.alianza.persistence.entities.{ Configuracion, PreguntaAutovalidacion }
import co.com.alianza.util.json.JsonUtil
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

  private def resolveObtenerPreguntas(preguntasEntities: List[PreguntaAutovalidacion], configuraciones: List[Configuracion]): Future[ResponseObtenerPreguntas] = Future {
    val preguntasDto: List[Pregunta] = preguntasEntities.map(pregunta => preguntasAutovalidacionDTO.entityToDto(pregunta))
    val numeroPreguntas: Int = obtenerValorEntero(configuraciones, ConfiguracionEnum.AUTOVALIDACION_NUMERO_PREGUNTAS.name)
    val numeroPreguntasLista: Int = obtenerValorEntero(configuraciones, ConfiguracionEnum.AUTOVALIDACION_NUMERO_PREGUNTAS_LISTA.name)
    ResponseObtenerPreguntas(Random.shuffle(preguntasDto).take(numeroPreguntasLista), numeroPreguntas)
  }

  private def obtenerValorEntero(configuraciones: Seq[Configuracion], llave: String): Int = {
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
      case TiposCliente.clienteAdministrador =>
        alianzaDao.getAdministratorClientQuestions(idUsuario)
      case _ => Future.failed(ValidacionException("", "Error tipo de cliente no valido."))
    }

    for {
      preguntas <- futurePreguntas
      configuraciones <- configuracionRepository.getAll()
      validar <- validarParametrizacion(preguntas.size, configuraciones.toList, llaveNumeroPreguntas)
      respuesta <- resolveObtenerPreguntasComprobar(preguntasAutovalidacionDTO.toPreguntaList(preguntas), configuraciones.toList)
    } yield respuesta

  }

  private def resolveObtenerPreguntasComprobar(preguntasDto: List[Pregunta], configuraciones: List[Configuracion]): Future[ResponseObtenerPreguntasComprobar] = Future {
    val numeroIntentos = obtenerValorEntero(configuraciones, ConfiguracionEnum.AUTOVALIDACION_NUMERO_REINTENTOS.name)
    val numeroPreguntasComprobacion = obtenerValorEntero(configuraciones, ConfiguracionEnum.AUTOVALIDACION_NUMERO_PREGUNTAS_COMPROBACION.name)
    val preguntasRandom = Random.shuffle(preguntasDto).take(numeroPreguntasComprobacion)
    ResponseObtenerPreguntasComprobar(preguntasRandom, numeroIntentos)
  }

  private def validarParametrizacion(numeroRespuestas: Int, configuraciones: List[Configuracion], llaveNumeroPreguntas: String): Future[Boolean] = {
    val numeroRespuestasParametrizadas = obtenerValorEntero(configuraciones, llaveNumeroPreguntas)
    val comparacion = numeroRespuestas == numeroRespuestasParametrizadas
    comparacion match {
      case true => Future.successful(comparacion)
      case false => Future.failed(ValidacionException("", "Error comprobacion parametrizacion campos"))
    }
  }

  def validarRespuestas(user: UsuarioAuth, respuestas: List[Respuesta], numeroIntentos: Int): Future[String] = {

    def futureRespuestas = user.tipoCliente match {
      case TiposCliente.clienteIndividual => alianzaDao.getIndividualClientQuestions(user.id)
      case TiposCliente.clienteAdministrador => alianzaDao.getAdministratorClientQuestions(user.id)
      case _ => Future.failed(ValidacionException("", "Error tipo de cliente no valido."))
    }

    val llaveReintentos: String = ConfiguracionEnum.AUTOVALIDACION_NUMERO_REINTENTOS.name
    val llavePreguntasCambio: String = ConfiguracionEnum.AUTOVALIDACION_NUMERO_PREGUNTAS_CAMBIAR.name
    val llavePreguntasComprobar: String = ConfiguracionEnum.AUTOVALIDACION_NUMERO_PREGUNTAS_COMPROBACION.name

    for {
      configuraciones <- configuracionRepository.getAll()
      validar <- validarParametrizacion(respuestas.size, configuraciones.toList, llavePreguntasComprobar)
      validarReintentos <- validarParametrizacion(numeroIntentos, configuraciones.toList, llaveReintentos)
      respuestasCompletas <- futureRespuestas
      respuesta <- validarRespuestasValidation(
        preguntasAutovalidacionDTO.toRespuestaCompletaList(respuestasCompletas),
        respuestas, obtenerValorEntero(configuraciones, llavePreguntasCambio)
      )
    } yield respuesta
  }

  /**
   * Validar respuestas y responder si no concuerdan
   * @param response
   * @param respuestas
   * @return
   */
  private def validarRespuestasValidation(response: Seq[RespuestaCompleta], respuestas: List[Respuesta], numeroPreguntasCambio: Int): Future[String] = {
    val respuestasGuardadas: Seq[Respuesta] = response.map(res => Respuesta(res.idPregunta, res.respuesta))
    //comprobar que las respuestas concuerden
    val existe: Boolean = respuestas.foldLeft(true)((existe, respuesta) => existe && respuestasGuardadas.contains(respuesta))
    existe match {
      case true => Future.successful("OK")
      case false => {
        //en caso que no concuerden, se envian la preguntas restantes mas una de las contestadas
        //1. obtener los ids de las respuestas
        val idsRespuesta: Seq[Int] = respuestas.map(_.idPregunta)
        //2. obtener los ids de las preguntas que se van a repetir
        val numeroPreguntasRepetidas: Int = respuestas.size - numeroPreguntasCambio
        val idsPreguntasRepetidas: Seq[Int] = Random.shuffle(idsRespuesta).take(numeroPreguntasRepetidas)
        //3. obtener ids de las preguntas que no corresponden a las preguntas contestadas
        val idsPreguntasNuevas: Seq[Int] = response.filter(res => !idsRespuesta.contains(res.idPregunta)).map(_.idPregunta)
        //4. obtener ids de las preguntas repetidas mas las preguntas nuevas
        val idsPreguntas: Seq[Int] = idsPreguntasRepetidas ++ Random.shuffle(idsPreguntasNuevas).take(numeroPreguntasCambio)
        //5. con los ids, obtener las preguntas a devolver
        val preguntas: Seq[Pregunta] = response.filter(res => (idsPreguntas).contains(res.idPregunta)).map(x => Pregunta(x.idPregunta, x.pregunta))
        //6. reenviar preguntas desordenadamente
        val preguntasRandom: String = JsonUtil.toJson(Random.shuffle(preguntas).take(preguntas.size))
        Future.failed(ValidacionException("", preguntasRandom))
      }
    }
  }

  override def bloquearRespuestas(idUsuario: Int, tipoCliente: TiposCliente): Future[Int] = {
    tipoCliente match {
      case TiposCliente.clienteIndividual => alianzaDao.deleteIndividualClientAnswers(idUsuario)
      case TiposCliente.clienteAdministrador => alianzaDao.bloquearRespuestasClienteAdministrador(idUsuario)
      case _ => Future.failed(ValidacionException("", "Error tipo de cliente no valido."))
    }
  }

}
