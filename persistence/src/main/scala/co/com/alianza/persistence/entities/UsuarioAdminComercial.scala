package co.com.alianza.persistence.entities

import java.sql.Timestamp
import CustomDriver.simple._

/**
 * Created by s4n on 2016
 */
case class UsuarioComercialAdmin(id: Int, correo: String, usuario: String, identificacion: String, tipoIdentificacion: Int, estado: Int,
  contrasena: Option[String], token: Option[String], ipUltimoIngreso: Option[String], fechaUltimoIngreso: Option[Timestamp], fechaActualizacion: Timestamp)

class UsuarioComercialAdminTable(tag: Tag) extends Table[UsuarioComercialAdmin](tag, "USUARIO_COMERCIAL_ADMIN") {
  def id = column[Int]("ID", O.PrimaryKey, O.AutoInc) // This is the primary key column
  def correo = column[String]("CORREO")
  def usuario = column[String]("USUARIO")
  def identificacion = column[String]("IDENTIFICACION")
  def tipoIdentificacion = column[Int]("TIPO_IDENTIFICACION")
  def estado = column[Int]("ESTADO")
  def contrasena = column[Option[String]]("CONTRASENA")
  def token = column[Option[String]]("TOKEN")
  def ipUltimoIngreso = column[Option[String]]("IP_ULTIMO_INGRESO")
  def fechaUltimoIngreso = column[Option[Timestamp]]("FECHA_ULTIMO_INGRESO")
  def fechaActualizacion = column[Timestamp]("FECHA_ACTUALIZACION")

  // Every table needs a * projection with the same type as the table's type parameter
  def * = (id, correo, usuario, identificacion, tipoIdentificacion, estado, contrasena, token, ipUltimoIngreso, fechaUltimoIngreso, fechaActualizacion) <> (
    UsuarioComercialAdmin.tupled, UsuarioComercialAdmin.unapply
  )
}