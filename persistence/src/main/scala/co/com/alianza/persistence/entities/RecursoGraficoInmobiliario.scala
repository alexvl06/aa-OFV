package co.com.alianza.persistence.entities

import co.com.alianza.persistence.entities.CustomDriver.simple._

/**
 * Created by alexandra in 2016.
 */
case class RecursoGraficoInmobiliario(id: Int, nombre: String, url: String, visibleMenu : Boolean)

case class RecursoGraficoInmobiliarioTable(tag: Tag) extends Table[RecursoGraficoInmobiliario](tag, "RECURSO_GRAFICO_INMOBILIARIO") {

  def id: Rep[Int] = column[Int]("ID", O.PrimaryKey, O.AutoInc)
  def titulo: Rep[String] = column[String]("TITULO")
  def url: Rep[String] = column[String]("URL_RECURSO_GRAFICO")
  def visible: Rep[Boolean] = column[Boolean]("VISIBLE")

  def * = (id, titulo, url, visible) <> (RecursoGraficoInmobiliario.tupled, RecursoGraficoInmobiliario.unapply)
}

