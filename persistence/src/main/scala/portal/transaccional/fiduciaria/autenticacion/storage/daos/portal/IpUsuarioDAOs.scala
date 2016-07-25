package portal.transaccional.fiduciaria.autenticacion.storage.daos.portal

import co.com.alianza.persistence.entities.IpsUsuario

import scala.concurrent.Future

/**
 * Created by hernando on 25/07/16.
 */
trait IpUsuarioDAOs {

  def getById(idUsuario: Int): Future[Seq[IpsUsuario]]

}
