package co.com.alianza.persistence.entities

import java.sql.Timestamp
import CustomDriver.simple._

/**
 * Created by manuel on 9/12/14.
 */
case class UsuarioEmpresarial(id: Int, correo: String, fechaActualizacion: Timestamp, identificacion: String, tipoIdentificacion: Int, usuario: String, estado: Int, contrasena: Option[String], token: Option[String], numeroIngresosErroneos:Int, ipUltimoIngreso: Option[String], fechaUltimoIngreso : Option[Timestamp], fechaUltimaPeticion: Option[Timestamp])

class UsuarioEmpresarialTable(tag: Tag) extends Table[UsuarioEmpresarial](tag, "USUARIO_EMPRESARIAL") {
  def id      = column[Int]("ID", O.PrimaryKey, O.AutoInc)
  def correo  = column[String]("CORREO")
  def fechaActualizacion   = column[Timestamp]("FECHA_ACTUALIZACION")
  def identificacion   = column[String]("IDENTIFICACION")
  def tipoIdentificacion   = column[Int]("TIPO_IDENTIFICACION")
  def usuario = column[String]("USUARIO")
  def estado   = column[Int]("ESTADO")
  def contrasena   = column[Option[String]]("CONTRASENA")
  def token   = column[Option[String]]("TOKEN")
  def numeroIngresosErroneos   = column[Int]("NUMERO_INGRESOS_ERRONEOS")
  def ipUltimoIngreso   = column[Option[String]]("IP_ULTIMO_INGRESO")
  def fechaUltimoIngreso   = column[Option[Timestamp]]("FECHA_ULTIMO_INGRESO")
  def fechaUltimaPeticion = column[Option[Timestamp]]("FECHA_ULTIMA_PETICION")

  def * =  (id, correo, fechaActualizacion, identificacion, tipoIdentificacion, usuario, estado, contrasena, token, numeroIngresosErroneos, ipUltimoIngreso, fechaUltimoIngreso, fechaUltimaPeticion) <> (UsuarioEmpresarial.tupled, UsuarioEmpresarial.unapply)
}