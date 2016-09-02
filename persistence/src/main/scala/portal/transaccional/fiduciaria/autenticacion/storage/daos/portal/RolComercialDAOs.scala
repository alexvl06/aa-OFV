package portal.transaccional.fiduciaria.autenticacion.storage.daos.portal

import co.com.alianza.persistence.entities.RolComercial

import scala.concurrent.Future

/**
 * Created by s4n on 2016
 */
trait RolComercialDAOs {

  def getAll(): Future[Seq[RolComercial]]

}
