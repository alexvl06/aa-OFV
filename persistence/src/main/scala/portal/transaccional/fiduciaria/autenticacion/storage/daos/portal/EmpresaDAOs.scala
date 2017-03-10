package portal.transaccional.fiduciaria.autenticacion.storage.daos.portal

import co.com.alianza.persistence.entities.Empresa

import scala.concurrent.Future

/**
 * Created by s4n on 2016
 */
trait EmpresaDAOs {

  def getByNit(nit: String): Future[Option[Empresa]]

}
