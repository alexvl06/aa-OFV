package co.com.alianza.persistence.entities

import co.com.alianza.persistence.entities.CustomDriver.simple._

/**
 * Created by alexandra in 2016.
 */
case class RecursoGraficoInmobiliario(id: Int, nombre: String, url: String, verAdmin : Boolean)

case class RecursoGraficoInmobiliarioTable(tag: Tag) extends Table[RecursoGraficoInmobiliario](tag, "RECURSO_BACKEND_INMOBILIARIO") {

  def id: Rep[Int] = column[Int]("ID", O.PrimaryKey, O.AutoInc)
  def titulo: Rep[String] = column[String]("TITULO")
  def url: Rep[String] = column[String]("URL_RECURSO")
  def verAdmin: Rep[Boolean] = column[Boolean]("VISIBLE_ADMIN")

  def pk = primaryKey("RECURSO_BACKEND_INMOBILIARIO_pkey", id)

  def * = (id, titulo, url, verAdmin) <> (RecursoGraficoInmobiliario.tupled, RecursoGraficoInmobiliario.unapply)
}
