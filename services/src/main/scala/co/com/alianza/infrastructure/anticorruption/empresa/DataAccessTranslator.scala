package co.com.alianza.infrastructure.anticorruption.empresa

import co.com.alianza.persistence.entities.{ Empresa => eEmpresa }
import co.com.alianza.infrastructure.dto.Empresa
/**
 * Created by manuel on 27/02/15.
 */
object DataAccessTranslator {

  def translateEmpresa(e: eEmpresa) = {
    import e._
    Empresa(id, nit, estadoEmpresa)
  }

}
