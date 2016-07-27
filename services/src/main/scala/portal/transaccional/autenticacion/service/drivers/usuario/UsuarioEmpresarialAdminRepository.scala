package portal.transaccional.autenticacion.service.drivers.usuario

import co.com.alianza.persistence.entities.UsuarioEmpresarialAdmin

import scala.concurrent.Future

/**
 * Created by hernando on 26/07/16.
 */
trait UsuarioEmpresarialAdminRepository {

  def getByIdentityAndUser(identificacion: String, usuario: String): Future[Option[UsuarioEmpresarialAdmin]]

}
