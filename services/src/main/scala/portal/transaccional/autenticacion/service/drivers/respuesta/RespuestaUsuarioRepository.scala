package portal.transaccional.autenticacion.service.drivers.respuesta

import co.com.alianza.commons.enumerations.TiposCliente._
import co.com.alianza.infrastructure.dto.Respuesta
import co.com.alianza.persistence.entities.RespuestasAutovalidacionUsuario

import scala.concurrent.Future

/**
 * Created by hernando on 25/07/16.
 */
trait RespuestaUsuarioRepository {

  def getRespuestasById(idUsuario: Int): Future[Seq[RespuestasAutovalidacionUsuario]]

  def guardarRespuestas(idUsuario: Int, tipoCliente: TiposCliente, respuestas: List[Respuesta]): Future[Option[Int]]

}
