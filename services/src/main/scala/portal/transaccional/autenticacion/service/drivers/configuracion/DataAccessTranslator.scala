package portal.transaccional.autenticacion.service.drivers.configuracion

import co.com.alianza.infrastructure.dto.Configuracion
import co.com.alianza.persistence.entities.{ Configuracion => eConfiguracion }

/**
 * Created by seven4n 2016
 */
object DataAccessTranslator {

  def entityToDto(e: eConfiguracion): Configuracion = Configuracion(e.llave, e.valor)

}
