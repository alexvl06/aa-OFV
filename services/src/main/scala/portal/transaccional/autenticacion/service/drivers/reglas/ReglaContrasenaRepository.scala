package portal.transaccional.autenticacion.service.drivers.reglas

import co.com.alianza.persistence.entities.{ ReglasContrasenas }

import scala.concurrent.Future

/**
 * Created by hernando on 25/07/16.
 */
trait ReglaContrasenaRepository {

  def getRegla(llave: String): Future[ReglasContrasenas]

}
