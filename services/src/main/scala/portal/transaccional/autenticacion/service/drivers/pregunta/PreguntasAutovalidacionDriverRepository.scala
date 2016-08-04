package portal.transaccional.autenticacion.service.drivers.pregunta

import akka.util.Timeout
import co.com.alianza.commons.enumerations.TiposCliente.TiposCliente
import co.com.alianza.exceptions.ValidacionException
import co.com.alianza.infrastructure.dto.{Configuracion, Pregunta, Respuesta, RespuestaCompleta}
import co.com.alianza.persistence.entities.{Configuraciones, PreguntaAutovalidacion}
import enumerations.ConfiguracionEnum
import portal.transaccional.autenticacion.service.drivers.configuracion.{ConfiguracionRepository, DataAccessTranslator => configuracionDTO}
import portal.transaccional.autenticacion.service.drivers.pregunta.{DataAccessTranslator => preguntasAutovalidacionDTO}
import portal.transaccional.autenticacion.service.web.preguntasAutovalidacion.{ResponseObtenerPreguntas, ResponseObtenerPreguntasComprobar}
import portal.transaccional.fiduciaria.autenticacion.storage.daos.portal.AlianzaDAOs

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
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

    /* //TODO: Implementar Tipos Cliente by: Jonathan*/
    /*val futurePreguntas = tipoCliente match {
      case TiposCliente.clienteIndividual => alianzaDao.getIndividualClientQuestions(idUsuario)
      case _ => Future(List.empty[Pregunta])
    }*/

    for {
      preguntas <- alianzaDao.getIndividualClientQuestions(idUsuario)
      configuraciones <- configuracionRepository.getAll()
      validar <- validarParametrizacion(preguntas.size, configuraciones.toList, llaveNumeroPreguntas)
      respuesta <- resolveObtenerPreguntasComprobar(preguntasAutovalidacionDTO.toPreguntaList(preguntas), configuraciones.toList)
    } yield respuesta

  }

  private def resolveObtenerPreguntasComprobar(preguntasDto: List[Pregunta], configuracionesEntities: List[Configuraciones]): Future[ResponseObtenerPreguntasComprobar] = Future {
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

  def validarRespuestas(idUsuario: Int, tipoCliente: TiposCliente, respuestas: List[Respuesta], numeroIntentos: Int): Future[Unit] = {

    /* //TODO: Implementar Tipos Cliente by: Jonathan
    def futureRespuestas: Future[Validation[PersistenceException, List[RespuestaCompleta]]] = message.tipoCliente match {
      case TiposCliente.clienteIndividual => DataAccessAdapter.obtenerRespuestaCompletaClienteIndividual(message.idUsuario)
      case TiposCliente.clienteAdministrador => DataAccessAdapter.obtenerRespuestaCompletaClienteAdministrador(message.idUsuario)
      case _ => Future(zSuccess(List.empty[RespuestaCompleta]))
    }
    */

    val llaveReintentos: String = ConfiguracionEnum.AUTOVALIDACION_NUMERO_REINTENTOS.name
    val llavePreguntasCambio: String = ConfiguracionEnum.AUTOVALIDACION_NUMERO_PREGUNTAS_CAMBIAR.name
    val llavePreguntasComprobar: String = ConfiguracionEnum.AUTOVALIDACION_NUMERO_PREGUNTAS_COMPROBACION.name

    for {
      configuraciones <- configuracionRepository.getAll()
      validar <- validarParametrizacion(respuestas.size, configuraciones.toList, llavePreguntasComprobar)
      validarReintentos <- validarParametrizacion(numeroIntentos, configuraciones.toList, llaveReintentos)
      respuestasCompletas <- alianzaDao.getIndividualClientQuestions(idUsuario)
      respuesta <- validarRespuestasValidation(preguntasAutovalidacionDTO.toRespuestaCompletaList(respuestasCompletas),
        respuestas, obtenerValorEntero(configuraciones.toList.map(conf => configuracionDTO.entityToDto(conf)), llavePreguntasCambio))
    } yield respuesta


    //resolveFutureValidation(future, (response: String) => response, errorValidacion, currentSender)
  }

  /**
    * Validar respuestas y responder si no concuerdan
    * @param response
    * @param respuestas
    * @return
    */
  private def validarRespuestasValidation(response: List[RespuestaCompleta], respuestas: List[Respuesta], numeroPreguntasCambio: Int): Future[String] = Future {
    val respuestasGuardadas: List[Respuesta] = response.map(res => Respuesta(res.idPregunta, res.respuesta))
    //comprobar que las respuestas concuerden
    val existe: Boolean = respuestas.foldLeft(true)((existe, respuesta) => existe && respuestasGuardadas.contains(respuesta))
    existe match {
      case true => "OK"
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
        (Random.shuffle(preguntas).take(preguntas.size)).toString()
      }
    }
  }

}
