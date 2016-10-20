package co.com.alianza.persistence.entities

import co.com.alianza.persistence.entities.CustomDriver.simple._

/**
 * Created by alexandra in 2016.
 */
case class RecursoGraficoInmobiliario(id: Int, nombre: String, url: String, rol : Int)

case class RecursoGraficoInmobiliarioTable(tag: Tag) extends Table[RecursoGraficoInmobiliario](tag, "RECURSO_GRAFICO_INMOBILIARIO") {

  def id: Rep[Int] = column[Int]("ID", O.PrimaryKey, O.AutoInc)
  def titulo: Rep[String] = column[String]("TITULO")
  def url: Rep[String] = column[String]("URL_RECURSO_GRAFICO")
  def rol: Rep[Int] = column[Int]("ROL")

  def * = (id, titulo, url, rol) <> (RecursoGraficoInmobiliario.tupled, RecursoGraficoInmobiliario.unapply)
}

