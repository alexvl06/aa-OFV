package portal.transaccional.autenticacion.service.drivers.configuracion

import co.com.alianza.persistence.entities.Configuraciones
import portal.transaccional.fiduciaria.autenticacion.storage.daos.portal.ConfiguracionDAOs

import scala.concurrent.{ ExecutionContext, Future }

/**
 * Created by hernando on 25/07/16.
 */
case class ConfiguracionDriverRepository(configuracionDAO: ConfiguracionDAOs)(implicit val ex: ExecutionContext) extends ConfiguracionRepository {

  def getConfiguracion(llave: String): Future[Configuraciones] = { configuracionDAO.getByKey(llave) }

}
