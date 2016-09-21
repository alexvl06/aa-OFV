package co.com.alianza.persistence.entities

import java.sql.Timestamp
import CustomDriver.simple._

/**
 * Created by alexandra in 2016
 */
case class UsuarioAgenteInmobiliario (id: Int, identificacion: String, tipoIdentificacion:Int, usuario: String, correo: String, estado: Int,
  contrasena: Option[String], token: Option[String], fechaActualizacion: Timestamp, numeroIngresosErroneos : Int , ipUltimoIngreso : Option[String],
  descripcion: String, fechaUltimoIngreso: Option[Timestamp]) extends UsuarioAgente


class UsuarioAgenteInmobiliarioTable (tag :Tag) extends UsuarioAgenteTable[UsuarioAgenteInmobiliario](tag, "USUARIO_AGENTE_INMOBILIARIO") {

  override val id = column[Int]("ID", O.PrimaryKey, O.AutoInc)
  override val identificacion = column[String]("NIT_EMPRESA")
  override val tipoIdentificacion = column[Int]("TIPO_IDENTIFICACION")
  override val usuario = column[String]("USUARIO")
  override val correo = column[String]("CORREO")
  override val estado = column[Int]("ESTADO")
  override val contrasena = column[Option[String]]("CONTRASENA")
  override val token = column[Option[String]]("TOKEN")
  override val fechaActualizacion = column[Timestamp]("FECHA_ACTUALIZACION")
  override val numeroIngresosErroneos = column[Int]("NUMERO_INGRESOS_ERRONEOS")
  override val ipUltimoIngreso = column[Option[String]]("IP_ULTIMO_INGRESO")
  override val descripcion = column[String]("DESCRIPCION")
  override val fechaUltimoIngreso = column[Option[Timestamp]]("FECHA_ULTIMO_INGRESO")

  def * = (id, identificacion, tipoIdentificacion, usuario, correo, estado,  contrasena, token, fechaActualizacion,numeroIngresosErroneos, ipUltimoIngreso,
    descripcion, fechaUltimoIngreso) <> (UsuarioAgenteInmobiliario.tupled, UsuarioAgenteInmobiliario.unapply)
}

