package co.com.alianza.infrastructure.anticorruption.permisos

import co.com.alianza.infrastructure.dto._
import co.com.alianza.persistence.entities.{
  PermisoTransaccionalUsuarioEmpresarial => ePermisoTransaccionalUsuarioEmpresarial,
  PermisoTransaccionalUsuarioEmpresarialAutorizador => ePermisoTransaccionalUsuarioEmpresarialAutorizador
}

/**
 * Created by manuel on 8/01/15.
 */
object PermisoTransaccionalDataAccessTranslator {

  def aDTO(e: ePermisoTransaccionalUsuarioEmpresarial) =
    PermisoTransaccionalUsuarioEmpresarial(e.idEncargo, e.idAgente, e.tipoTransaccion, e.tipoPermiso, e.montoMaximoTransaccion, e.montoMaximoDiario, e.minimoNumeroPersonas)

  def aEntity(dto: PermisoTransaccionalUsuarioEmpresarial) =
    ePermisoTransaccionalUsuarioEmpresarial(dto.idEncargo, dto.idAgente, dto.tipoTransaccion , dto.tipoPermiso, dto.montoMaximoTransaccion, dto.montoMaximoDiario, dto.minimoNumeroPersonas)

  def aEncargoPermisosDTO(e: (ePermisoTransaccionalUsuarioEmpresarial, List[ePermisoTransaccionalUsuarioEmpresarialAutorizador])) =
    EncargoPermisos(
      wspf_plan = e._1.idEncargo,
      permisos = e._2 map {aut => aPermisoTransaccionalUsuarioEmpresarialAgentes(e)}
    )

  def aPermisoTransaccionalUsuarioEmpresarialAgentes(permiso: (ePermisoTransaccionalUsuarioEmpresarial, List[ePermisoTransaccionalUsuarioEmpresarialAutorizador])) =
    PermisoTransaccionalUsuarioEmpresarialAgentes(Some(aDTO(permiso._1)), Some(permiso._2.map(aAgenteDTO)))

  def aAgenteDTO (a: ePermisoTransaccionalUsuarioEmpresarialAutorizador) = Agente(a.idAutorizador)

}
