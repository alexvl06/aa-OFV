package co.com.alianza.persistence.entities

import co.com.alianza.persistence.entities.CustomDriver.simple._

/**
 * Created by alexandra in 2016.
 */
case class RecursoGraficoInmobiliario(id: Int, nombre: String, url: String, verMenu : Boolean, verAdmin : Boolean)

case class RecursoGraficoInmobiliarioTable(tag: Tag) extends Table[RecursoGraficoInmobiliario](tag, "RECURSO_GRAFICO_INMOBILIARIO") {

  def id: Rep[Int] = column[Int]("ID", O.PrimaryKey, O.AutoInc)
  def titulo: Rep[String] = column[String]("TITULO")
  def url: Rep[String] = column[String]("URL_RECURSO_GRAFICO")
  def verMenu: Rep[Boolean] = column[Boolean]("VISIBLE_MENU")
  def verAdmin: Rep[Boolean] = column[Boolean]("VISIBLE_ADMIN")

  def * = (id, titulo, url, verMenu, verAdmin) <> (RecursoGraficoInmobiliario.tupled, RecursoGraficoInmobiliario.unapply)
}

