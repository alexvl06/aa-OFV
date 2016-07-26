package portal.transaccional.fiduciaria.autenticacion.storage.daos.portal

import co.com.alianza.persistence.entities.Empresa

import scala.concurrent.Future

/**
 * Created by hernando on 26/07/16.
 */
trait EmpresaDAOs {

  def getByIdentity(nit: String): Future[Option[Empresa]]

}
