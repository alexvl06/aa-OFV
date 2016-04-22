package co.com.alianza.persistence.entities

import CustomDriver.simple._

/**
 * Created by manuel on 8/01/15.
 */
case class PermisoTransaccionalUsuarioEmpresarial(idEncargo: String, idAgente: Int, tipoTransaccion: Int, tipoPermiso: Int, montoMaximoTransaccion: Option[Double], montoMaximoDiario: Option[Double], minimoNumeroPersonas: Option[Int])

class PermisoTransaccionalUsuarioEmpresarialTable(tag: Tag) extends Table[PermisoTransaccionalUsuarioEmpresarial](tag, "PERMISO_TX_USUARIO_EMPRESARIAL") {

  def idEncargo = column[String]("ID_ENCARGO")
  def idAgente = column[Int]("ID_USUARIO_EMPRESARIAL")
  def tipoTransaccion = column[Int]("TIPO_TRANSACCION")
  def tipoPermiso = column[Int]("TIPO_PERMISO")
  def montoMaximoTransaccion = column[Option[Double]]("MONTO_MAXIMO_TRANSACCION")
  def montoMaximoDiario = column[Option[Double]]("MONTO_MAXIMO_DIARIO")
  def minimoNumeroPersonas = column[Option[Int]]("MINIMO_NUMERO_PERSONAS")

  def pk = primaryKey("PERMISO_TX_USUARIO_EMPRESARIAL_PK", (idEncargo, idAgente, tipoTransaccion))

  def * = (idEncargo, idAgente, tipoTransaccion, tipoPermiso, montoMaximoTransaccion, montoMaximoDiario, minimoNumeroPersonas) <> (PermisoTransaccionalUsuarioEmpresarial.tupled, PermisoTransaccionalUsuarioEmpresarial.unapply)
}