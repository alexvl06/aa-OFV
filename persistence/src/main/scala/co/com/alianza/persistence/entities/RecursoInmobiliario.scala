package co.com.alianza.persistence.entities

import co.com.alianza.persistence.entities.CustomDriver.simple._

/**
 * Created by alexandra in 2016.
 */
case class RecursoInmobiliario(idGrafico: Int, idBacken: Int)

case class RecursoInmobiliarioTable(tag: Tag) extends Table[RecursoInmobiliario](tag, "RECURSO_INMOBILIARIO") {

  def idGrafico: Rep[Int] = column[Int]("ID_RECURSO_GRAFICO")
  def idBacken: Rep[Int] = column[Int]("ID_RECURSO_BACKEND")

  def pk = primaryKey("RECURSO_INMOBILIARIO_PKEY", (idGrafico, idBacken))

  def * = (idGrafico, idBacken) <> (RecursoInmobiliario.tupled, RecursoInmobiliario.unapply)
}

