package co.com.alianza.persistence.entities

import java.sql.Timestamp
import CustomDriver.simple._

case class PinUsuario(id: Option[Int], idUsuario: Int, token: String, fechaExpiracion: Timestamp, tokenHash: String)

class PinUsuarioTable(tag: Tag) extends Table[PinUsuario](tag, "PIN_USUARIO") {
  def id = column[Option[Int]]("ID", O.PrimaryKey, O.AutoInc)
  def idUsuario = column[Int]("ID_USUARIO")
  def token = column[String]("TOKEN")
  def fechaExpiracion = column[Timestamp]("FECHA_EXPIRACION")
  def tokenHash = column[String]("TOKEN_HASH")

  def * = (id, idUsuario, token, fechaExpiracion, tokenHash) <> (PinUsuario.tupled, PinUsuario.unapply)
}