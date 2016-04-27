package co.com.alianza.persistence.entities

import java.sql.Timestamp
import CustomDriver.simple._

/**
 * Created by manuel on 18/12/14.
 */
case class PinUsuarioEmpresarialAdmin(id: Option[Int], idUsuario: Int, token: String, fechaExpiracion: Timestamp, tokenHash: String)

class PinUsuarioEmpresarialAdminTable(tag: Tag) extends Table[PinUsuarioEmpresarialAdmin](tag, "PIN_USUARIO_EMPRESARIAL_ADMIN") {
  def id = column[Option[Int]]("ID", O.PrimaryKey, O.AutoInc)
  def idUsuario = column[Int]("ID_USUARIO_EMPRESARIAL_ADMIN")
  def token = column[String]("TOKEN")
  def fechaExpiracion = column[Timestamp]("FECHA_EXPIRACION")
  def tokenHash = column[String]("TOKEN_HASH")

  def * = (id, idUsuario, token, fechaExpiracion, tokenHash) <> (PinUsuarioEmpresarialAdmin.tupled, PinUsuarioEmpresarialAdmin.unapply)
}
