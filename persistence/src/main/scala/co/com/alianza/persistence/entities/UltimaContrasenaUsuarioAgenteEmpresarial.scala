package co.com.alianza.persistence.entities

import java.sql.Timestamp

import CustomDriver.simple._

/**
 * Created by manuel on 7/01/15.
 */
case class UltimaContrasenaUsuarioAgenteEmpresarial(
  id: Option[Int],
  idUsuario: Int,
  contrasena: String,
  fechaUltimaContrasena: Timestamp
)

class UltimaContrasenaUsuarioAgenteEmpresarialTable(tag: Tag) extends Table[UltimaContrasenaUsuarioAgenteEmpresarial](tag, "ULTIMAS_CONTRASENAS_USUARIO_EMPRESARIAL") {

  def id = column[Option[Int]]("ID", O.PrimaryKey, O.AutoInc) // This is the primary key column

  def idUsuario = column[Int]("ID_USUARIO")

  def contrasena = column[String]("CONTRASENA")

  def fechaUltimaContrasena = column[Timestamp]("FECHA_ULTIMA_CONTRASENA")

  // Every table needs a * projection with the same type as the table's type parameter
  def * = (id, idUsuario, contrasena, fechaUltimaContrasena) <> (UltimaContrasenaUsuarioAgenteEmpresarial.tupled, UltimaContrasenaUsuarioAgenteEmpresarial.unapply)
}