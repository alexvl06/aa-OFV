package co.com.alianza.infrastructure.anticorruption.permisos

import co.com.alianza.infrastructure.dto._
import co.com.alianza.persistence.entities.{
  PermisoAgente => ePermisoAgente,
  PermisoAgenteAutorizador => ePermisoAgenteAutorizador,
  PermisoTransaccionalUsuarioEmpresarial => ePermisoTxUsuarioEmpresarial,
  PermisoTransaccionalUsuarioEmpresarialAutorizador => ePermisoTxUsuarioEmpresarialAutorizador
}

/**
 * Created by manuel on 8/01/15.
 */
object PermisoTransaccionalDataAccessTranslator {
  
  
  type permisosEncargos = List[(ePermisoAgente, List[(Option[ePermisoAgenteAutorizador], Option[Boolean])])]
  type permisosGenerales =  List[
    (String,
      List[(ePermisoTxUsuarioEmpresarial,
        List[(Option[ePermisoTxUsuarioEmpresarialAutorizador],
          Option[Boolean])])])]

  def aDTO(e: ePermisoTxUsuarioEmpresarial): PermisoTransaccionalUsuarioEmpresarial =
    PermisoTransaccionalUsuarioEmpresarial(
      e.idEncargo, e.idAgente, e.tipoTransaccion, e.tipoPermiso, e.montoMaximoTransaccion, e.montoMaximoDiario, e.minimoNumeroPersonas)

  def aEntity(dto: PermisoTransaccionalUsuarioEmpresarial): ePermisoTxUsuarioEmpresarial =
    ePermisoTxUsuarioEmpresarial(
      dto.idEncargo, dto.idAgente, dto.tipoTransaccion, dto.tipoPermiso, dto.montoMaximoTransaccion, dto.montoMaximoDiario, dto.minimoNumeroPersonas)

  def aEntity(dto: PermisoAgente): ePermisoAgente =
    ePermisoAgente(dto.idAgente, dto.tipoTransaccion, dto.minimoNumeroPersonas, dto.tipoPermiso, dto.montoMaximoTransaccion, dto.montoMaximoDiario)

  def aEncargoPermisosDTO(idEncargo: String,
    e: List[(ePermisoTxUsuarioEmpresarial, List[(Option[ePermisoTxUsuarioEmpresarialAutorizador], Option[Boolean])])]): EncargoPermisos =
    EncargoPermisos( wspf_plan = idEncargo, permisos = e map aPermisoTransaccionalUsuarioEmpresarialAgentes )

  def aPermisoTransaccionalUsuarioEmpresarialAgentes(permiso: (ePermisoTxUsuarioEmpresarial,
    List[(Option[ePermisoTxUsuarioEmpresarialAutorizador], Option[Boolean])])): PermisoTransaccionalUsuarioEmpresarialAgentes = {
    PermisoTransaccionalUsuarioEmpresarialAgentes(
      Some(aDTO(permiso._1)),
      if (permiso._2.isEmpty) None else Some(permiso._2.filter { _._1.isDefined }.map{ o => aAgenteDTO(o._1.get, o._2) }))
  }

  def aAgenteDTO(a: ePermisoTxUsuarioEmpresarialAutorizador, esAdmin: Option[Boolean]): Autorizador = Autorizador(a.idAutorizador, esAdmin)

  def ListarAutorizadores(autorizadores: List[(Option[ePermisoAgenteAutorizador], Option[Boolean])]): Option[List[Autorizador]] = {
    if (autorizadores.isEmpty){
      None
    }  else {
      Some(autorizadores.filter( n => n._1.isDefined).map( o => aAutorizadorDTO(o._1.get, o._2) ))
    }
  }

  def aPermisos( permisos: permisosEncargos, encargosPermisos: permisosGenerales): (List[Permiso], List[EncargoPermisos]) = {

    (permisos.map(
      pa =>
        Permiso(
          Some(
            PermisoAgente(
              pa._1.idAgente,
              pa._1.tipoTransaccion,
              pa._1.minimoNumeroPersonas,
              pa._1.tipoPermiso,
              pa._1.montoMaximoTransaccion,
              pa._1.montoMaximoDiario)),
          ListarAutorizadores(pa._2))),
      encargosPermisos.map(ep => aEncargoPermisosDTO(ep._1, ep._2)))
  }

  def aAutorizadorDTO(paa: ePermisoAgenteAutorizador, esAdmin: Option[Boolean]): Autorizador = Autorizador(paa.idAutorizador, esAdmin)

}
