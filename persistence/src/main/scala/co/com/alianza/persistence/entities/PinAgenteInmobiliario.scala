package co.com.alianza.persistence.entities

import java.sql.Timestamp
import CustomDriver.simple._

case class PinAgenteInmobiliario(id: Option[Int], idAgente: Int, token: String, fechaExpiracion: Timestamp, tokenHash: String, uso: Int)

class PinAgenteInmobiliarioTable(tag: Tag) extends Table[PinAgenteInmobiliario](tag, "PIN_AGENTE_INMOBILIARIO") {
  def id = column[Option[Int]]("ID", O.PrimaryKey, O.AutoInc)
  def idAgente = column[Int]("ID_AGENTE_INMOBILIARIO")
  def token = column[String]("TOKEN")
  def fechaExpiracion = column[Timestamp]("FECHA_EXPIRACION")
  def tokenHash = column[String]("TOKEN_HASH")
  def uso = column[Int]("USO")

  def * = (id, idAgente, token, fechaExpiracion, tokenHash, uso) <> (PinAgenteInmobiliario.tupled, PinAgenteInmobiliario.unapply)
}
