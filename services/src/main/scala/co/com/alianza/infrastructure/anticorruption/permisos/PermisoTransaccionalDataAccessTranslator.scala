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

  def aEncargoPermisosDTO(idEncargo: String, e: List[(ePermisoTransaccionalUsuarioEmpresarial, List[(Option[ePermisoTransaccionalUsuarioEmpresarialAutorizador], Option[Boolean])])]) =
    EncargoPermisos(
      wspf_plan = idEncargo,
      permisos = e map aPermisoTransaccionalUsuarioEmpresarialAgentes
    )

  def aPermisoTransaccionalUsuarioEmpresarialAgentes(permiso: (ePermisoTransaccionalUsuarioEmpresarial, List[(Option[ePermisoTransaccionalUsuarioEmpresarialAutorizador], Option[Boolean])])) =
    PermisoTransaccionalUsuarioEmpresarialAgentes(Some(aDTO(permiso._1)), if(permiso._2.isEmpty) None else Some(permiso._2.filter{ _._1.isDefined }.map{ o => aAgenteDTO(o._1.get, o._2) }))

  def aAgenteDTO (a: ePermisoTransaccionalUsuarioEmpresarialAutorizador, esAdmin: Option[Boolean]) = Autorizador(a.idAutorizador, esAdmin)

}
