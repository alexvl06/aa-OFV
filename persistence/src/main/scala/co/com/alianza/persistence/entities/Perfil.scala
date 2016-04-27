package co.com.alianza.persistence.entities

import CustomDriver.simple._

/**
 *
 * @author smontanez
 */
case class Perfil(id: Int, nombre: String)

class PerfilTable(tag: Tag) extends Table[Perfil](tag, "PERFIL") {
  def id = column[Int]("ID", O.PrimaryKey, O.AutoInc)
  def nombre = column[String]("NOMBRE")

  def * = (id, nombre) <> (Perfil.tupled, Perfil.unapply)
}