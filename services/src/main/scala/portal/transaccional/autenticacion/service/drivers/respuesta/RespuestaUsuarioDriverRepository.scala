package portal.transaccional.autenticacion.service.drivers.respuesta

import co.com.alianza.exceptions.ValidacionException
import co.com.alianza.infrastructure.dto.Respuesta
import co.com.alianza.persistence.entities.RespuestasAutovalidacionUsuario
import enumerations.ConfiguracionEnum
import portal.transaccional.autenticacion.service.drivers.configuracion.ConfiguracionRepository
import portal.transaccional.fiduciaria.autenticacion.storage.daos.portal.RespuestaUsuarioDAOs
import scala.concurrent.{ExecutionContext, Future}

/**
 * Created by hernando on 25/07/16.
 */
case class RespuestaUsuarioDriverRepository(respuestaDAO: RespuestaUsuarioDAOs,
                                            configuracionRepository: ConfiguracionRepository)
                                           (implicit val ex: ExecutionContext) extends RespuestaUsuarioRepository {

  def getRespuestasById(idUsuario: Int): Future[Seq[RespuestasAutovalidacionUsuario]] = {
    respuestaDAO.getById(idUsuario)
  }

  def guardarRespuestas(idUsuario: Int, respuestas: List[Respuesta]): Future[Option[Int]] = {
    val llave = ConfiguracionEnum.AUTOVALIDACION_NUMERO_PREGUNTAS.name
    val respuestasPersistencia = respuestas.map(x => new RespuestasAutovalidacionUsuario(x.idPregunta, idUsuario, x.respuesta))
    for {
      configuracion <- configuracionRepository.getConfiguracion(llave)
      validar <- validarParametrizacion(respuestasPersistencia.size, configuracion.valor.toInt)
      borrar  <- respuestaDAO.delete(idUsuario)
      guardar <- respuestaDAO.insert(respuestasPersistencia)
    } yield  guardar
  }

  /**
    * Validaciones correspondiente a que el numero de respuestas sea igual a
    * el numero de preguntas parametrizadas por el administrador
    * @param numeroRespuestas
    * @param numeroRespuestasParametrizadas
    * @return
    */
  private def validarParametrizacion(numeroRespuestas: Int, numeroRespuestasParametrizadas: Int): Future[Boolean] = {
    val comparacion = numeroRespuestas == numeroRespuestasParametrizadas
    comparacion match {
      case true => Future.successful(comparacion)
      case false => Future.failed(ValidacionException("", "Error comprobacion parametrizacion campos"))
    }
  }
}
