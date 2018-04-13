package co.com.alianza.persistence.entities

import java.sql.Timestamp
import CustomDriver.simple._

/**
 * Created by s4n on 2014
 */
case class UsuarioEmpresarial(id: Int, correo: String, fechaActualizacion: Timestamp, identificacion: String, tipoIdentificacion: Int, usuario: String,
    estado: Int, contrasena: Option[String], token: Option[String], numeroIngresosErroneos: Int, ipUltimoIngreso: Option[String],
    fechaUltimoIngreso: Option[Timestamp], nombreUsuario: String, cargo: String, descripcion: Option[String], interventor1: Option[Boolean] = Some(false)) extends UsuarioAgente {
  interventor = interventor1.getOrElse(false)
}

class UsuarioEmpresarialTable(tag: Tag) extends UsuarioAgenteTable[UsuarioEmpresarial](tag, "USUARIO_EMPRESARIAL") {

  override val id = column[Int]("ID", O.PrimaryKey, O.AutoInc)
  override val correo = column[String]("CORREO")
  override val fechaActualizacion = column[Timestamp]("FECHA_ACTUALIZACION")
  override val identificacion = column[String]("IDENTIFICACION")
  override val tipoIdentificacion = column[Int]("TIPO_IDENTIFICACION")
  override val usuario = column[String]("USUARIO")
  override val estado = column[Int]("ESTADO")
  override val contrasena = column[Option[String]]("CONTRASENA")
  override val token = column[Option[String]]("TOKEN")
  override val numeroIngresosErroneos = column[Int]("NUMERO_INGRESOS_ERRONEOS")
  override val ipUltimoIngreso = column[Option[String]]("IP_ULTIMO_INGRESO")
  override val fechaUltimoIngreso = column[Option[Timestamp]]("FECHA_ULTIMO_INGRESO")
  val nombreUsuario = column[String]("NOMBRE")
  val cargo = column[String]("CARGO")
  override val descripcion = column[Option[String]]("DESCRIPCION")
  val interventor = column[Option[Boolean]]("INTERVENTOR")

  def * = (id, correo, fechaActualizacion, identificacion, tipoIdentificacion, usuario, estado, contrasena, token,
    numeroIngresosErroneos, ipUltimoIngreso, fechaUltimoIngreso, nombreUsuario, cargo, descripcion, interventor) <>
    (UsuarioEmpresarial.tupled, UsuarioEmpresarial.unapply)

}