package co.com.alianza.persistence.entities

import java.sql.Timestamp

import CustomDriver.simple._
/**
 * Created by S4N on 14/11/14.
 */
case class UltimaContrasena(
  id: Option[Int],
  idUsuario: Int,
  contrasena: String,
  fechaUltimaContrasena: Timestamp
)

class UltimaContrasenaTable(tag: Tag) extends Table[UltimaContrasena](tag, "ULTIMAS_CONTRASENAS") {

  def id = column[Option[Int]]("ID", O.PrimaryKey, O.AutoInc) // This is the primary key column

  def idUsuario = column[Int]("ID_USUARIO")

  def contrasena = column[String]("CONTRASENA")

  def fechaUltimaContrasena = column[Timestamp]("FECHA_ULTIMA_CONTRASENA")

  // Every table needs a * projection with the same type as the table's type parameter
  def * = (id, idUsuario, contrasena, fechaUltimaContrasena) <> (UltimaContrasena.tupled, UltimaContrasena.unapply)
}