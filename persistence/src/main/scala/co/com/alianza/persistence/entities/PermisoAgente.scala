package co.com.alianza.persistence.entities

import CustomDriver.simple._

/**
 * Created by manuel on 16/02/15.
 */
case class PermisoAgente (idAgente: Int, tipoTransaccion: Int, minimoNumeroPersonas: Option[Int])

class PermisoAgenteTable (tag: Tag) extends Table[PermisoAgente](tag, "PERMISO_AGENTE") {

  def idAgente   = column[Int]("ID_AGENTE")
  def tipoTransaccion   = column[Int]("TIPO_TRANSACCION")
  def minimoNumeroPersonas   = column[Option[Int]]("MINIMO_NUMERO_PERSONAS")

  def pk = primaryKey("PERMISO_AGENTE_PK", (idAgente, tipoTransaccion))

  def * = (idAgente, tipoTransaccion, minimoNumeroPersonas) <> (PermisoAgente.tupled, PermisoAgente.unapply)
}
