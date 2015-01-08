package co.com.alianza.persistence.entities

import CustomDriver.simple._

/**
 * Created by manuel on 8/01/15.
 */
case class PermisoTransaccionalUsuarioEmpresarial (idEncargo: String, idAgente: Int, tipo: Int, montoMaximoTransaccion: Option[Double], montoMaximoDiario: Option[Double], minimoNumeroPersonas: Option[Int])

class PermisoTransaccionalUsuarioEmpresarialTable (tag: Tag) extends Table[PermisoTransaccionalUsuarioEmpresarial](tag, "PERMISO_TX_USUARIO_EMPRESARIAL") {

  def idEncargo  = column[String]("ID_ENCARGO")
  def idAgente   = column[Int]("ID_USUARIO_EMPRESARIAL")
  def tipo   = column[Int]("TIPO")
  def montoMaximoTransaccion   = column[Option[Double]]("MONTO_MAXIMO_TRANSACCION")
  def montoMaximoDiario   = column[Option[Double]]("MONTO_MAXIMO_DIARIO")
  def minimoNumeroPersonas   = column[Option[Int]]("TIPO_IDENTIFICACION")

  def pk = primaryKey("PERMISO_TX_USUARIO_EMPRESARIAL_PK", (idEncargo, idAgente))

  def * = (idEncargo, idAgente, tipo, montoMaximoTransaccion, montoMaximoDiario, minimoNumeroPersonas) <> (PermisoTransaccionalUsuarioEmpresarial.tupled, PermisoTransaccionalUsuarioEmpresarial.unapply)
}