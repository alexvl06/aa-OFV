package co.com.alianza.persistence.entities

import co.com.alianza.persistence.entities.CustomDriver.simple._

/**
 * Created by alexandra in 2016.
 */
case class RecursoAgenteInmobiliario(id: Int, nombre: String, url: String)

case class RecursoAgenteInmobiliarioTable(tag: Tag) extends Table[RecursoAgenteInmobiliario](tag, "RECURSO_GRAFICO_AGENTE_INMOBILIARIO") {

  def id: Rep[Int] = column[Int]("ID", O.PrimaryKey, O.AutoInc)
  def titulo: Rep[String] = column[String]("TITULO")
  def url: Rep[String] = column[String]("URL_RECURSO_GRAFICO")

  def * = (id, titulo, url) <> (RecursoAgenteInmobiliario.tupled, RecursoAgenteInmobiliario.unapply)
}

