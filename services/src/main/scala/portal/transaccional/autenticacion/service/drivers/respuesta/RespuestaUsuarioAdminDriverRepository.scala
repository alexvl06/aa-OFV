package portal.transaccional.autenticacion.service.drivers.respuesta

import co.com.alianza.persistence.entities.RespuestasAutovalidacionUsuario
import portal.transaccional.fiduciaria.autenticacion.storage.daos.portal.RespuestaUsuarioDAOs

import scala.concurrent.{ Future, ExecutionContext }

/**
 * Created by hernando on 25/07/16.
 */
case class RespuestaUsuarioAdminDriverRepository(respuestaDAO: RespuestaUsuarioDAOs)(implicit val ex: ExecutionContext) extends RespuestaUsuarioRepository {

  def getRespuestasById(idUsuario: Int): Future[Seq[RespuestasAutovalidacionUsuario]] = {
    respuestaDAO.getById(idUsuario)
  }

}
