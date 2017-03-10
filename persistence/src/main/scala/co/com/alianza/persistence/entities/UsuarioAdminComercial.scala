package co.com.alianza.persistence.entities

import java.sql.Timestamp
import CustomDriver.simple._

/**
 * Created by s4n on 2016
 */
case class UsuarioComercialAdmin(id: Int, correo: String, usuario: String, contrasena: Option[String], token: Option[String], ipUltimoIngreso: Option[String],
  fechaUltimoIngreso: Option[Timestamp], fechaActualizacion: Timestamp, name: Option[String])

class UsuarioComercialAdminTable(tag: Tag) extends Table[UsuarioComercialAdmin](tag, "USUARIO_COMERCIAL_ADMIN") {
  def id = column[Int]("ID", O.PrimaryKey, O.AutoInc) // This is the primary key column
  def usuario = column[String]("USUARIO")
  def contrasena = column[Option[String]]("CONTRASENA")
  def name = column[Option[String]]("NOMBRE")
  def correo = column[String]("CORREO")
  def token = column[Option[String]]("TOKEN")
  def ipUltimoIngreso = column[Option[String]]("IP_ULTIMO_INGRESO")
  def fechaUltimoIngreso = column[Option[Timestamp]]("FECHA_ULTIMO_INGRESO")
  def fechaActualizacion = column[Timestamp]("FECHA_ACTUALIZACION")

  // Every table needs a * projection with the same type as the table's type parameter
  def * = (id, correo, usuario, contrasena, token, ipUltimoIngreso, fechaUltimoIngreso, fechaActualizacion, name) <> (
    UsuarioComercialAdmin.tupled, UsuarioComercialAdmin.unapply
  )
}