package portal.transaccional.autenticacion.service.drivers.configuracion

import co.com.alianza.persistence.entities.Configuraciones

import scala.concurrent.Future

trait ConfiguracionRepository {

  def getConfiguracion(llave: String): Future[Configuraciones]

  def getAll(): Future[Seq[Configuraciones]]

}
