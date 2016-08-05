package co.com.alianza.persistence.entities

import java.sql.Timestamp
import CustomDriver.simple._

/**
 * Created by s4n on 2016
 */
case class UsuarioComercial(id: Int, usuario: String, token: Option[String], ipUltimoIngreso: Option[String], fechaUltimoIngreso: Option[Timestamp])

class UsuarioComercialTable(tag: Tag) extends Table[UsuarioComercial](tag, "USUARIO_COMERCIAL") {
  def id = column[Int]("ID", O.PrimaryKey, O.AutoInc)
  def usuario = column[String]("USUARIO", O.PrimaryKey, O.AutoInc)
  def token = column[Option[String]]("TOKEN")
  def ipUltimoIngreso = column[Option[String]]("IP_ULTIMO_INGRESO")
  def fechaUltimoIngreso = column[Option[Timestamp]]("FECHA_ULTIMO_INGRESO")

  def * = (id, usuario, token, ipUltimoIngreso, fechaUltimoIngreso) <> (UsuarioComercial.tupled, UsuarioComercial.unapply)

}