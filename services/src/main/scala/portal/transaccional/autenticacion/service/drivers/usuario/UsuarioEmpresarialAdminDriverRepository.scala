package portal.transaccional.autenticacion.service.drivers.usuario

import co.com.alianza.persistence.entities.UsuarioEmpresarialAdmin
import portal.transaccional.fiduciaria.autenticacion.storage.daos.portal.UsuarioEmpresarialAdminDAOs

import scala.concurrent.{ Future, ExecutionContext }

/**
 * Created by hernando on 26/07/16.
 */
case class UsuarioEmpresarialAdminDriverRepository(usuarioDAO: UsuarioEmpresarialAdminDAOs)(implicit val ex: ExecutionContext)
    extends UsuarioEmpresarialAdminRepository {

  def getByIdentificacion(identificacion: String, usuario: String): Future[Option[UsuarioEmpresarialAdmin]] = {
    usuarioDAO.getByIdentityAndUser(identificacion, usuario)
  }

  /**
   * Invalidar el token al usuario
   * @param token
   * @return
   */
  def invalidarToken(token: String): Future[Int] = {
    usuarioDAO.deleteToken(token)
  }

}
