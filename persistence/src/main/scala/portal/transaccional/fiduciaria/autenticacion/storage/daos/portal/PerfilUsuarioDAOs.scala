package portal.transaccional.fiduciaria.autenticacion.storage.daos.portal

import co.com.alianza.persistence.entities.PerfilUsuario

import scala.concurrent.Future

/**
 * Created by alexandra on 3/08/16.
 */
trait PerfilUsuarioDAOs {

  def create(perfiles: Seq[PerfilUsuario]): Future[Option[Int]]

  def getProfileByUsuario(idUsuario: Int): Future[Option[PerfilUsuario]]
}
