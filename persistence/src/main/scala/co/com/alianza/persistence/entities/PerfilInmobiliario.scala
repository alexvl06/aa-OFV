package co.com.alianza.persistence.entities

import co.com.alianza.persistence.entities.CustomDriver.simple._

/**
 * Created by alexandra on 22/10/16.
 */

case class PerfilInmobiliario(id: Int, nombre: String)

class PerfilInmobiliarioTable(tag: Tag) extends Table[PerfilInmobiliario](tag, "PERFIL_INMOBILIARIO") {

  def id: Rep[Int] = column[Int]("ID", O.PrimaryKey, O.AutoInc)
  def nombre: Rep[String] = column[String]("NOMBRE")

  def * = (id, nombre) <> (PerfilInmobiliario.tupled, PerfilInmobiliario.unapply)
}