package co.com.alianza.persistence.entities

import java.sql.Timestamp
import CustomDriver.simple._

/**
 * Created by s4n on 2016
 */
case class UsuarioComercial(id: Int, correo: String, fechaActualizacion: Timestamp, identificacion: String, tipoIdentificacion: Int, estado: Int,
  contrasena: String, token: Option[String], numeroIngresosErroneos: Int, ipUltimoIngreso: Option[String], fechaUltimoIngreso: Option[Timestamp])

class UsuarioComercialTable(tag: Tag) extends Table[UsuarioComercial](tag, "USUARIO_COMERCIAL") {
  def id = column[Int]("ID", O.PrimaryKey, O.AutoInc)
  def correo = column[String]("CORREO")
  def fechaActualizacion = column[Timestamp]("FECHA_ACTUALIZACION")
  def identificacion = column[String]("IDENTIFICACION")
  def tipoIdentificacion = column[Int]("TIPO_IDENTIFICACION")
  def estado = column[Int]("ESTADO")
  def contrasena = column[String]("CONTRASENA")
  def token = column[Option[String]]("TOKEN")
  def numeroIngresosErroneos = column[Int]("NUMERO_INGRESOS_ERRONEOS")
  def ipUltimoIngreso = column[Option[String]]("IP_ULTIMO_INGRESO")
  def fechaUltimoIngreso = column[Option[Timestamp]]("FECHA_ULTIMO_INGRESO")

  def * = (id, correo, fechaActualizacion, identificacion, tipoIdentificacion, estado, contrasena, token, numeroIngresosErroneos, ipUltimoIngreso,
    fechaUltimoIngreso) <> (UsuarioComercial.tupled, UsuarioComercial.unapply)
}