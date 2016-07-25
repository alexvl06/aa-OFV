package portal.transaccional.autenticacion.service.drivers.reglas

import co.com.alianza.persistence.entities.ReglasContrasenas
import portal.transaccional.fiduciaria.autenticacion.storage.daos.portal.ReglaContrasenaDAOs

import scala.concurrent.{ ExecutionContext, Future }

/**
 * Created by hernando on 25/07/16.
 */
case class ReglaContrasenaDriverRepository(reglaDAO: ReglaContrasenaDAOs)(implicit val ex: ExecutionContext) extends ReglaContrasenaRepository {

  def getRegla(llave: String): Future[ReglasContrasenas] = {
    reglaDAO.getByKey(llave)
  }

}
