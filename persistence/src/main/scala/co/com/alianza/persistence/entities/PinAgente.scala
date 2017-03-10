package co.com.alianza.persistence.entities

/**
 * Created by S4N on 22/12/14.
 */

import java.sql.Timestamp
import CustomDriver.simple._

case class PinAgente(id: Option[Int], idUsuarioEmpresarial: Int, token: String, fechaExpiracion: Timestamp, tokenHash: String, uso: Int)

class PinEmpresaTable(tag: Tag) extends Table[PinAgente](tag, "PIN_EMPRESA") {
  def id = column[Option[Int]]("ID", O.PrimaryKey, O.AutoInc)
  def idUsuarioEmpresarial = column[Int]("ID_USUARIO_EMPRESARIAL")
  def token = column[String]("TOKEN")
  def fechaExpiracion = column[Timestamp]("FECHA_EXPIRACION")
  def tokenHash = column[String]("TOKEN_HASH")
  def uso = column[Int]("USO")

  def * = (id, idUsuarioEmpresarial, token, fechaExpiracion, tokenHash, uso) <> (PinAgente.tupled, PinAgente.unapply)
}