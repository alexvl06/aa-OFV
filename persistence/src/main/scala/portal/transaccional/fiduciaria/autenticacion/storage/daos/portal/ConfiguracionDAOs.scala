package portal.transaccional.fiduciaria.autenticacion.storage.daos.portal

import co.com.alianza.persistence.entities.Configuracion

import scala.concurrent.Future

/**
 * Created by hernando on 25/07/16.
 */
trait ConfiguracionDAOs {

  def getAll(): Future[Seq[Configuracion]]

  def getByKey(llave: String): Future[Configuracion]

}
