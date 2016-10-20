package co.com.alianza.persistence.entities

import co.com.alianza.persistence.entities.CustomDriver.simple._

/**
 * Created by alexandra in 2016.
 */
case class RecursoBackendInmobiliario(id: Int, url: String, rol : Int)

case class RecursoBackendInmobiliarioTable(tag: Tag) extends Table[RecursoBackendInmobiliario](tag, "RECURSO_BACKEND_INMOBILIARIO") {

  def id: Rep[Int] = column[Int]("ID", O.PrimaryKey, O.AutoInc)
  def url: Rep[String] = column[String]("URL_RECURSO")
  def rol: Rep[Int] = column[Int]("ROL")

  def pk = primaryKey("RECURSO_BACKEND_INMOBILIARIO_pkey", id)

  def * = (id, url, rol) <> (RecursoBackendInmobiliario.tupled, RecursoBackendInmobiliario.unapply)
}
