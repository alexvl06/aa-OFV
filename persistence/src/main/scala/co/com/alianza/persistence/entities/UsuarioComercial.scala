package co.com.alianza.persistence.entities

import java.sql.Timestamp
import CustomDriver.simple._

/**
 * Created by s4n on 2016
 */
case class UsuarioComercial(id: Int, correo: String, identificacion: String, tipoIdentificacion: Int, estado: Int, token: Option[String],
  ipUltimoIngreso: Option[String], fechaUltimoIngreso: Option[Timestamp])

class UsuarioComercialTable(tag: Tag) extends Table[UsuarioComercial](tag, "USUARIO_COMERCIAL") {
  def id = column[Int]("ID", O.PrimaryKey, O.AutoInc)
  def correo = column[String]("CORREO")
  def identificacion = column[String]("IDENTIFICACION")
  def tipoIdentificacion = column[Int]("TIPO_IDENTIFICACION")
  def estado = column[Int]("ESTADO")
  def token = column[Option[String]]("TOKEN")
  def ipUltimoIngreso = column[Option[String]]("IP_ULTIMO_INGRESO")
  def fechaUltimoIngreso = column[Option[Timestamp]]("FECHA_ULTIMO_INGRESO")

  def * = (id, correo, identificacion, tipoIdentificacion, estado, token, ipUltimoIngreso, fechaUltimoIngreso) <> (
    UsuarioComercial.tupled,
    UsuarioComercial.unapply
  )
}