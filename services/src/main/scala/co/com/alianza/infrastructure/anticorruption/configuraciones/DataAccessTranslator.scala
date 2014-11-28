package co.com.alianza.infrastructure.anticorruption.configuraciones

import co.com.alianza.persistence.entities.{Configuraciones => dConfiguracion}
import co.com.alianza.infrastructure.dto.Configuracion
import java.util.Date
import java.sql.Timestamp

/**
 *
 * @author seven4n
 */
object  DataAccessTranslator {

  def translateConfiguracion(configuraciones:List[dConfiguracion]) = {
    configuraciones map ( conf => Configuracion( conf.llave, conf.valor ))
  }

  def translateConfiguracion(conf:dConfiguracion) = {
    Configuracion( conf.llave, conf.valor )
  }

}
