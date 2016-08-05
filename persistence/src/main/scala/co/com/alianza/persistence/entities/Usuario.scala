package co.com.alianza.persistence.entities

import java.sql.Timestamp
import CustomDriver.simple._

/**
 *
 * @author seven4n
 */

case class Usuario(id: Int, correo: String, expiracionContraseña: Timestamp, identificacion: String, tipoIdentificacion: Int, estado: Int,
  contrasena: Option[String], token: Option[String], numeroIngresosErroneos: Int, ipUltimoIngreso: Option[String], fechaUltimoIngreso: Option[Timestamp])

class UsuarioTable(tag: Tag) extends Table[Usuario](tag, "USUARIO") {
  def id = column[Option[Int]]("ID", O.PrimaryKey, O.AutoInc)
  def correo = column[String]("CORREO")
  def expiracionContraseña = column[Timestamp]("EXPIRACION_CONTRASEÑA")
  def identificacion = column[String]("IDENTIFICACION")
  def tipoIdentificacion = column[Int]("TIPO_IDENTIFICACION")
  def estado = column[Int]("ESTADO")
  def contrasena = column[Option[String]]("CONTRASENA")
  def token = column[Option[String]]("TOKEN")
  def numeroIngresosErroneos = column[Int]("NUMERO_INGRESOS_ERRONEOS")
  def ipUltimoIngreso = column[Option[String]]("IP_ULTIMO_INGRESO")
  def ultimoIngreso = column[Option[Timestamp]]("FECHA_ULTIMO_INGRESO")

  def * = (id, correo, expiracionContraseña, identificacion, tipoIdentificacion, estado, contrasena, token,
    numeroIngresosErroneos, ipUltimoIngreso, ultimoIngreso) <> (Usuario.tupled, Usuario.unapply)
}