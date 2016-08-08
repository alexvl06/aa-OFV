package portal.transaccional.fiduciaria.autenticacion.storage.daos.portal

import co.com.alianza.persistence.entities.ReglasContrasenas

import scala.concurrent.Future

/**
 * Created by hernando on 25/07/16.
 */
trait ReglaContrasenaDAOs {

  def getAll(): Future[Seq[ReglasContrasenas]]

  def getByKey(llave: String): Future[ReglasContrasenas]

}
