package portal.transaccional.autenticacion.service.drivers.preguntasAutovalidacion

import akka.util.Timeout
import co.com.alianza.infrastructure.dto.Configuracion
import co.com.alianza.persistence.entities.{ Configuraciones, PreguntaAutovalidacion }
import enumerations.ConfiguracionEnum
import portal.transaccional.autenticacion.service.drivers.configuracion.ConfiguracionRepository
import portal.transaccional.autenticacion.service.drivers.pregunta.PreguntasRepository
import portal.transaccional.autenticacion.service.drivers.preguntasAutovalidacion.{ DataAccessTranslator => preguntasAutovalidacionDTO }
import portal.transaccional.autenticacion.service.drivers.configuracion.{ DataAccessTranslator => configuracionDTO }
import portal.transaccional.autenticacion.service.web.preguntasAutovalidacion.PreguntasResponse

import scala.concurrent.duration._
import scala.concurrent.{ ExecutionContext, Future }
import scala.util._

case class PreguntasAutovalidacionDriverRepository(preguntasRepository: PreguntasRepository, configuracionRepository: ConfiguracionRepository)(implicit val ex: ExecutionContext) extends PreguntasAutovalidacionRepository {

  implicit val timeout = Timeout(5.seconds)

  /**
   * Obtener las preguntas disponibles
   * El numero de preguntas que se envia, es igual a el numero de preguntas
   * que debe aparecer en la lista desplegable.
   */
  def obtenerPreguntas(): Future[PreguntasResponse] = {
    for {
      preguntas <- preguntasRepository.obtenerPreguntas()
      configuraciones <- configuracionRepository.getAll()
      respuesta <- resolveObtenerPreguntas(preguntas.toList, configuraciones.toList)
    } yield respuesta
  }

  private def resolveObtenerPreguntas(preguntasEntities: List[PreguntaAutovalidacion], configuracionesEntities: List[Configuraciones]): Future[PreguntasResponse] = Future {
    val preguntasDto = preguntasEntities.map(pregunta => preguntasAutovalidacionDTO.entityToDto(pregunta))
    val configuracionesDto = configuracionesEntities.map(conf => configuracionDTO.entityToDto(conf))

    val numeroPreguntas = obtenerValorEntero(configuracionesDto, ConfiguracionEnum.AUTOVALIDACION_NUMERO_PREGUNTAS.name)
    val numeroPreguntasLista = obtenerValorEntero(configuracionesDto, ConfiguracionEnum.AUTOVALIDACION_NUMERO_PREGUNTAS_LISTA.name)
    val preguntas = Random.shuffle(preguntasDto).take(numeroPreguntasLista)
    PreguntasResponse(preguntasDto, numeroPreguntas)
  }

  private def obtenerValorEntero(configuraciones: List[Configuracion], llave: String): Int = {
    configuraciones.filter(conf => conf.llave.equals(llave)).head.valor.toInt
  }

}
