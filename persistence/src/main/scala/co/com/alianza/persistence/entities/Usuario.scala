package co.com.alianza.persistence.entities

import java.sql.Timestamp
import CustomDriver.simple._

/**
 *
 * @author seven4n
 */

case class Usuario(id: Option[Int], correo: String, fechaActualizacion: Timestamp, identificacion: String, tipoIdentificacion: Int, estado: Int, contrasena: String, token: Option[String], numeroIngresosErroneos:Int)

class UsuarioTable(tag: Tag) extends Table[Usuario](tag, "USUARIO") {
  def id      = column[Option[Int]]("ID", O.PrimaryKey, O.AutoInc) // This is the primary key column
  def correo  = column[String]("CORREO")
  //TODO:Cambiar nombrecolumna
  def fechaActualizacion   = column[Timestamp]("FECHA_CADUCIDAD")
  def identificacion   = column[String]("IDENTIFICACION")
  def tipoIdentificacion   = column[Int]("TIPO_IDENTIFICACION")
  def estado   = column[Int]("ESTADO")
  def contrasena   = column[String]("CONTRASENA")
  def token   = column[Option[String]]("TOKEN")
  def numeroIngresosErroneos   = column[Int]("NUMERO_INGRESOS_ERRONEOS")

  // Every table needs a * projection with the same type as the table's type parameter
  def * =  (id, correo, fechaActualizacion, identificacion, tipoIdentificacion, estado, contrasena, token, numeroIngresosErroneos) <> (Usuario.tupled, Usuario.unapply)
}