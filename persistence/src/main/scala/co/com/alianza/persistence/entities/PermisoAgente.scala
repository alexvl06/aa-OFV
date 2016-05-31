package co.com.alianza.persistence.entities

import CustomDriver.simple._

/**
 * Created by manuel on 16/02/15.
 */
case class PermisoAgente(idAgente: Int, tipoTransaccion: Int, minimoNumeroPersonas: Option[Int], tipoPermiso: Int, montoMaximoTransaccion: Option[Double],
  montoMaximoDiario: Option[Double])

class PermisoAgenteTable(tag: Tag) extends Table[PermisoAgente](tag, "PERMISO_AGENTE") {

  def idAgente = column[Int]("ID_AGENTE")
  def tipoTransaccion = column[Int]("TIPO_TRANSACCION")
  def minimoNumeroPersonas = column[Option[Int]]("MINIMO_NUMERO_PERSONAS")
  def tipoPermiso = column[Int]("TIPO_PERMISO")
  def montoMaximoTransaccion = column[Option[Double]]("MONTO_MAXIMO_TRANSACCION")
  def montoMaximoDiario = column[Option[Double]]("MONTO_MAXIMO_DIARIO")

  def pk = primaryKey("PERMISO_AGENTE_PK", (idAgente, tipoTransaccion))

  def * = (idAgente, tipoTransaccion, minimoNumeroPersonas, tipoPermiso, montoMaximoTransaccion, montoMaximoDiario) <> (PermisoAgente.tupled, PermisoAgente.unapply)
}
