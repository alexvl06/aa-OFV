package co.com.alianza.persistence.entities

import co.com.alianza.persistence.entities.CustomDriver.simple._

/**
 * Created by alexandra in 2016.
 */
case class RecursoBackendInmobiliario(id: Int, url: String)

case class RecursoBackendInmobiliarioTable(tag: Tag) extends Table[RecursoBackendInmobiliario](tag, "RECURSO_BACKEND_INMOBILIARIO") {

  def id: Rep[Int] = column[Int]("ID", O.PrimaryKey, O.AutoInc)
  def url: Rep[String] = column[String]("URL_RECURSO")

  def * = (id, url) <> (RecursoBackendInmobiliario.tupled, RecursoBackendInmobiliario.unapply)
}
