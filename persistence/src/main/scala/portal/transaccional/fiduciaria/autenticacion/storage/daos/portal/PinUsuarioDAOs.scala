package portal.transaccional.fiduciaria.autenticacion.storage.daos.portal

import co.com.alianza.persistence.entities.PinUsuario

import scala.concurrent.Future

trait PinUsuarioDAOs {

  def create(pinUsuario: PinUsuario): Future[Int]

}
