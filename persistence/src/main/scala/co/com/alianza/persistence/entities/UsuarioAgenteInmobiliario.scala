package co.com.alianza.persistence.entities

import java.sql.Timestamp
import CustomDriver.simple._

/**
 * Created by alexandra on 2016
 */
case class UsuarioAgenteInmobiliario (id: Int, idEmpresa: String, usuario: String, estado: Int, contrasena: Option[String], token: Option[String],
  fechaActualizacion: Timestamp, numeroIngresosErroneos : Int , descripcion: String)


class UsuarioAgenteInmobiliarioTable (tag :Tag) extends Table[UsuarioAgenteInmobiliario](tag, "USUARIO_AGENTE_INMOBILIARIO") {

  def id = column[Int]("ID", O.PrimaryKey, O.AutoInc)
  def idEmpresa = column[String]("ID_EMPRESA")
  def usuario = column[String]("USUARIO")
  def fechaActualizacion = column[Timestamp]("FECHA_ACTUALIZACION")
  def estado = column[Int]("ESTADO")
  def contrasena = column[Option[String]]("CONTRASENA")
  def token = column[Option[String]]("TOKEN")
  def numeroIngresosErroneos = column[Int]("NUMERO_INGRESOS_ERRONEOS")
  def descripcion = column[String]("DESCRIPCION")

  def * = (id, idEmpresa, usuario, estado, contrasena, token, fechaActualizacion, numeroIngresosErroneos, descripcion) <>
    (UsuarioAgenteInmobiliario.tupled, UsuarioAgenteInmobiliario.unapply)
}

