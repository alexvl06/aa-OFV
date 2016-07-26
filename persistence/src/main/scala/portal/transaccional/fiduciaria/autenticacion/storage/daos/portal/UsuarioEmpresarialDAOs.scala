package portal.transaccional.fiduciaria.autenticacion.storage.daos.portal

import co.com.alianza.persistence.entities.UsuarioEmpresarial

import scala.concurrent.Future

/**
 * Created by hernando on 26/07/16.
 */
trait UsuarioEmpresarialDAOs {

  def getByIdentity(numeroIdentificacion: String): Future[Option[UsuarioEmpresarial]]

}
