package portal.transaccional.autenticacion.service.drivers.configuracion

import co.com.alianza.infrastructure.dto.Configuracion

import scala.concurrent.{ Future, ExecutionContext }

/**
 * Created by hernando on 25/07/16.
 */
case class ConfiguracionDriverRepository(implicit val ex: ExecutionContext) extends ConfiguracionRepository {

  //def buscarConfiguracion(llave: String): Future[Configuracion]

}
