package portal.transaccional.autenticacion.service.drivers.configuracion

import co.com.alianza.persistence.entities.Configuracion
import enumerations.ConfiguracionEnum

import scala.concurrent.Future

trait ConfiguracionRepository {

  def getConfiguracion(configuracion: ConfiguracionEnum.Val): Future[Configuracion]

  def getAll(): Future[Seq[Configuracion]]

}
