package portal.transaccional.autenticacion.service.drivers.empresa

import co.com.alianza.infrastructure.dto.Empresa
import co.com.alianza.persistence.entities.{ Empresa => eEmpresarial }

/**
 * Created by seven4n 2016
 */
object DataAccessTranslator {

  def entityToDto(e: eEmpresarial): Empresa = Empresa(e.id, e.nit, e.estadoEmpresa)

}
