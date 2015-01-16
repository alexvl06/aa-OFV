package co.com.alianza.infrastructure.dto

/**
 * Created by manuel on 8/01/15.
 */
case class PermisoTransaccionalUsuarioEmpresarial (
                                                    idEncargo: String, idAgente: Int, tipoTransaccion: Int,
                                                    tipoPermiso: Int, montoMaximoTransaccion: Option[Double],
                                                    montoMaximoDiario: Option[Double], minimoNumeroPersonas: Option[Int], seleccionado: Boolean = false)

case class EncargoPermisos (wspf_plan: String, permisos: List[PermisoTransaccionalUsuarioEmpresarialAgentes])

case class PermisoTransaccionalUsuarioEmpresarialAgentes(permiso: Option[PermisoTransaccionalUsuarioEmpresarial],
                                                         agentes: Option[List[Agente]] = None)
case class Agente(id: Int)

