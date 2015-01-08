package co.com.alianza.infrastructure.anticorruption.permisos

import co.com.alianza.infrastructure.dto.PermisoTransaccionalUsuarioEmpresarial
import co.com.alianza.persistence.entities.{PermisoTransaccionalUsuarioEmpresarial => ePermisoTransaccionalUsuarioEmpresarial}

/**
 * Created by manuel on 8/01/15.
 */
object PermisoTransaccionalDataAccessTranslator {

  def aDTO(e: ePermisoTransaccionalUsuarioEmpresarial) =
    PermisoTransaccionalUsuarioEmpresarial(e.idEncargo, e.idAgente, e.tipo, e.montoMaximoTransaccion, e.montoMaximoDiario, e.minimoNumeroPersonas)

  def aEntity(dto: PermisoTransaccionalUsuarioEmpresarial) =
    ePermisoTransaccionalUsuarioEmpresarial(dto.idEncargo, dto.idAgente, dto.tipo, dto.montoMaximoTransaccion, dto.montoMaximoDiario, dto.minimoNumeroPersonas)

}
