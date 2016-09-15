package co.com.alianza.persistence.entities

import java.sql.Timestamp
import CustomDriver.simple._

/**
 * Created by alexandra on 2016
 */

abstract class Agente {
  def id: Int
  def identificacion : String
  def usuario: String
  def correo:String
  def estado: Int
  def contrasena: Option[String]
  def token: Option[String]
  def fechaActualizacion: Timestamp
  def numeroIngresosErroneos: Int
  def ipUltimoIngreso: Option[String]
  def fechaUltimoIngreso: Option[Timestamp]
  def tipoIdentificacion: Int
  def descripcion: String
}

case class UsuarioAgenteInmobiliario (id: Int, identificacion: String, tipoIdentificacion:Int, usuario: String, correo: String, estado: Int,
  contrasena: Option[String], token: Option[String], fechaActualizacion: Timestamp, numeroIngresosErroneos : Int , ipUltimoIngreso : Option[String],
  descripcion: String, fechaUltimoIngreso: Option[Timestamp]) extends Agente


class UsuarioAgenteInmobiliarioTable (tag :Tag) extends Table[UsuarioAgenteInmobiliario](tag, "USUARIO_AGENTE_INMOBILIARIO") {

  def id = column[Int]("ID", O.PrimaryKey, O.AutoInc)
  def identificacion = column[String]("NIT_EMPRESA")
  def tipoIdentificacion = column[Int]("TIPO_IDENTIFICACION")
  def usuario = column[String]("USUARIO")
  def correo = column[String]("CORREO")
  def estado = column[Int]("ESTADO")
  def contrasena = column[Option[String]]("CONTRASENA")
  def token = column[Option[String]]("TOKEN")
  def fechaActualizacion = column[Timestamp]("FECHA_ACTUALIZACION")
  def numeroIngresosErroneos = column[Int]("NUMERO_INGRESOS_ERRONEOS")
  def ipUltimoIngreso = column[Option[String]]("IP_ULTIMO_INGRESO")
  def descripcion = column[String]("DESCRIPCION")
  def fechaUltimoIngreso = column[Option[Timestamp]]("FECHA_ULTIMO_INGRESO")

  def * = (id, identificacion, tipoIdentificacion, usuario, correo, estado,  contrasena, token, fechaActualizacion,numeroIngresosErroneos, ipUltimoIngreso, descripcion,
    fechaUltimoIngreso) <> (UsuarioAgenteInmobiliario.tupled, UsuarioAgenteInmobiliario.unapply)
}

