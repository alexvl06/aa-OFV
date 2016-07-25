package portal.transaccional.autenticacion.service.drivers.configuracion

import co.com.alianza.infrastructure.dto.Configuracion
import co.com.alianza.persistence.entities.Configuraciones

import scala.concurrent.Future

/**
 * Created by hernando on 25/07/16.
 */
trait ConfiguracionRepository {

  def getConfiguracion(llave: String): Future[Configuraciones]

}
