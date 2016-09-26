package co.com.alianza.persistence.entities

import java.sql.Timestamp
import CustomDriver.simple._

/**
 * Created by alexandra on 2016
 */
case class UltimaContrasenaAgenteInmobiliario(id: Option[Int], idUsuario: Int, contrasena: String, fechaUltimaContrasena: Timestamp)

class UltimaContrasenaUsuarioAgenteInmobiliarioTable(tag: Tag) extends Table[UltimaContrasenaAgenteInmobiliario](tag,
  "ULTIMAS_CONTRASENAS_USUARIO_AGENTE_INMOBILIARIO") {

  def id: Rep[Option[Int]] = column[Option[Int]]("ID", O.PrimaryKey, O.AutoInc)
  def idUsuario: Rep[Int] = column[Int]("ID_USUARIO")
  def contrasena: Rep[String] = column[String]("CONTRASENA")
  def fechaUltimaContrasena: Rep[Timestamp] = column[Timestamp]("FECHA_ULTIMA_CONTRASENA")

  def * = (id, idUsuario, contrasena, fechaUltimaContrasena) <> (UltimaContrasenaAgenteInmobiliario.tupled, UltimaContrasenaAgenteInmobiliario.unapply)
}