package co.com.alianza.persistence.entities

import CustomDriver.simple._

case class TipoValidacion(id: Int, descripcion: String, isParametrizable: Boolean)

class TipoValidacionTable(tag: Tag) extends Table[TipoValidacion](tag, "TIPO_VALIDACION") {

  def id = column[Int]("ID_TIPO_VALIDACION", O.PrimaryKey)
  def descripcion = column[String]("VALIDACION_DESC")
  def isParametrizable = column[Boolean]("ES_PARAMETRIZABLE")

  def * = (id, descripcion, isParametrizable) <> (TipoValidacion.tupled, TipoValidacion.unapply)
}
