package portal.transaccional.fiduciaria.autenticacion.storage.daos.portal

import co.com.alianza.persistence.entities.PinAdmin

import scala.concurrent.Future

/**
 * Created by hernando on 26/10/16.
 */
trait PinAdminDAOs {

  def create(pinUsuario: PinAdmin): Future[Int]

  def findById(token: String): Future[Option[PinAdmin]]

  def delete(token: String): Future[Int]

}
