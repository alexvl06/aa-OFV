package portal.transaccional.autenticacion.service.drivers.respuesta

import co.com.alianza.commons.enumerations.TiposCliente.TiposCliente
import co.com.alianza.infrastructure.dto.Respuesta
import co.com.alianza.persistence.entities.RespuestasAutovalidacionUsuario
import portal.transaccional.fiduciaria.autenticacion.storage.daos.portal.RespuestaUsuarioDAOs

import scala.concurrent.{ExecutionContext, Future}

/**
 * Created by hernando on 25/07/16.
 */
case class RespuestaUsuarioAdminDriverRepository(respuestaDAO: RespuestaUsuarioDAOs)(implicit val ex: ExecutionContext) extends RespuestaUsuarioRepository {

  def getRespuestasById(idUsuario: Int): Future[Seq[RespuestasAutovalidacionUsuario]] = {
    respuestaDAO.getById(idUsuario)
  }

  override def guardarRespuestas(idUsuario: Int, tipoCliente: TiposCliente, respuestas: List[Respuesta]): Future[Option[Int]] = ???
}
