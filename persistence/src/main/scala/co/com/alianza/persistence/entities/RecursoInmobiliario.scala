package co.com.alianza.persistence.entities

import co.com.alianza.persistence.entities.CustomDriver.simple._

/**
 * Created by alexandra in 2016.
 */
case class RecursoGraficoInmobiliario(idGrafico: Int, idBacken: String)

case class RecursoGraficoInmobiliarioTable(tag: Tag) extends Table[RecursoGraficoInmobiliario](tag, "RECURSO_INMOBILIARIO") {

  def idGrafico: Rep[String] = column[String]("ID_RECURSO_GRAFICO")
  def idBacken: Rep[String] = column[String]("ID_RECURSO_BACKEND")

  def pk = primaryKey("RECURSO_INMOBILIARIO_PKEY", (idGrafico,idBacken))

  def * = (idGrafico, idBacken) <> (RecursoGraficoInmobiliario.tupled, RecursoGraficoInmobiliario.unapply)
}

