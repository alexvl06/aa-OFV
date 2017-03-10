package co.com.alianza.persistence.entities

import CustomDriver.simple._

case class RecursoComercial(id: Option[Int], nombre: String, descripcion: String)

class RecursoComercialTable(tag: Tag) extends Table[RecursoComercial](tag, "RECURSO_COMERCIAL") {

  def id = column[Option[Int]]("ID", O.PrimaryKey, O.AutoInc)
  def nombre = column[String]("NOMBRE")
  def descripcion = column[String]("DESCRIPCION")

  def * = (id, nombre, descripcion) <> (RecursoComercial.tupled, RecursoComercial.unapply)
}

