package co.com.alianza.persistence.entities

import CustomDriver.simple._

case class RecursoBackend(id: Int, url: String)

class RecursoBackendTable(tag: Tag) extends Table[RecursoBackend](tag, "RECURSO_BACKEND") {
  def id = column[Int]("ID_RECURSO", O.PrimaryKey)
  def url = column[String]("URL_RECURSO")

  def * = (id, url) <> (RecursoBackend.tupled, RecursoBackend.unapply)

}
