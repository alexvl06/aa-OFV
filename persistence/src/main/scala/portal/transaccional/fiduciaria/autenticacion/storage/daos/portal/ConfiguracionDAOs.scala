package portal.transaccional.fiduciaria.autenticacion.storage.daos.portal

import co.com.alianza.persistence.entities.Configuraciones

import scala.concurrent.Future

/**
 * Created by hernando on 25/07/16.
 */
trait ConfiguracionDAOs {

  def getAll(): Future[Seq[Configuraciones]]

  def getByKey(llave: String): Future[Configuraciones]

}
