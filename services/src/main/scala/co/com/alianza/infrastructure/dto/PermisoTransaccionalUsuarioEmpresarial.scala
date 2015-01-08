package co.com.alianza.infrastructure.dto

/**
 * Created by manuel on 8/01/15.
 */
case class PermisoTransaccionalUsuarioEmpresarial (idEncargo: String, idAgente: Int, tipoTransaccion: Int, tipoPermiso: Int, montoMaximoTransaccion: Option[Double], montoMaximoDiario: Option[Double], minimoNumeroPersonas: Option[Int])
