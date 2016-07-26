package portal.transaccional.autenticacion.service.drivers.usuario

import co.com.alianza.persistence.entities.UsuarioEmpresarialAdmin
import portal.transaccional.fiduciaria.autenticacion.storage.daos.portal.UsuarioEmpresarialAdminDAOs

import scala.concurrent.{ Future, ExecutionContext }

/**
 * Created by hernando on 26/07/16.
 */
case class UsuarioEmpresarialAdminDriverRepository(usuarioDAO: UsuarioEmpresarialAdminDAOs)(implicit val ex: ExecutionContext)
  extends UsuarioEmpresarialAdminRepository {

  def getByIdentificacion(numeroIdentificacion: String): Future[Option[UsuarioEmpresarialAdmin]] = {
    usuarioDAO.getByIdentity(numeroIdentificacion)
  }

}
