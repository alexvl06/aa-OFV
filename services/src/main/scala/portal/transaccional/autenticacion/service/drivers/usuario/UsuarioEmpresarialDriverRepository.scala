package portal.transaccional.autenticacion.service.drivers.usuario

import co.com.alianza.persistence.entities.UsuarioEmpresarial
import portal.transaccional.fiduciaria.autenticacion.storage.daos.portal.UsuarioEmpresarialDAOs

import scala.concurrent.{ Future, ExecutionContext }

/**
 * Created by hernando on 26/07/16.
 */
case class UsuarioEmpresarialDriverRepository(usuarioDAO: UsuarioEmpresarialDAOs)(implicit val ex: ExecutionContext) extends UsuarioEmpresarialRepository {

  def getByIdentificacion(numeroIdentificacion: String): Future[Option[UsuarioEmpresarial]] = {
    usuarioDAO.getByIdentity(numeroIdentificacion)
  }

}
