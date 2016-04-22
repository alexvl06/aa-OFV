package co.com.alianza.persistence.entities

import java.sql.Timestamp
import CustomDriver.simple._

/**
 * Created by manuel on 17/12/14.
 */
case class UsuarioEmpresarialAdmin(id: Int, correo: String, fechaActualizacion: Timestamp, usuario: String, identificacion: String, tipoIdentificacion: Int, estado: Int, contrasena: Option[String], token: Option[String], numeroIngresosErroneos: Int, ipUltimoIngreso: Option[String], fechaUltimoIngreso: Option[Timestamp])

class UsuarioEmpresarialAdminTable(tag: Tag) extends Table[UsuarioEmpresarialAdmin](tag, "USUARIO_EMPRESARIAL_ADMIN") {
  def id = column[Int]("ID", O.PrimaryKey, O.AutoInc) // This is the primary key column
  def correo = column[String]("CORREO")
  def fechaActualizacion = column[Timestamp]("FECHA_ACTUALIZACION")
  def usuario = column[String]("USUARIO")
  def identificacion = column[String]("IDENTIFICACION")
  def tipoIdentificacion = column[Int]("TIPO_IDENTIFICACION")
  def estado = column[Int]("ESTADO")
  def contrasena = column[Option[String]]("CONTRASENA")
  def token = column[Option[String]]("TOKEN")
  def numeroIngresosErroneos = column[Int]("NUMERO_INGRESOS_ERRONEOS")
  def ipUltimoIngreso = column[Option[String]]("IP_ULTIMO_INGRESO")
  def fechaUltimoIngreso = column[Option[Timestamp]]("FECHA_ULTIMO_INGRESO")
  def tokenActivacion = column[Option[Timestamp]]("TOKEN_ACTIVACION")

  // Every table needs a * projection with the same type as the table's type parameter
  def * = (id, correo, fechaActualizacion, usuario, identificacion, tipoIdentificacion, estado, contrasena, token, numeroIngresosErroneos, ipUltimoIngreso, fechaUltimoIngreso) <> (UsuarioEmpresarialAdmin.tupled, UsuarioEmpresarialAdmin.unapply)
}