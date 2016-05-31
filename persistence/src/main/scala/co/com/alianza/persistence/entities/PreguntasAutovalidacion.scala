package co.com.alianza.persistence.entities

import CustomDriver.simple._

/**
 *
 * @author seven4n
 */

case class PreguntasAutovalidacion(id: Int, pregunta: String)

class PreguntasAutovalidacionTable(tag: Tag) extends Table[PreguntasAutovalidacion](tag, "PREGUNTAS_AUTOVALIDACION") {
  def id = column[Int]("ID", O.PrimaryKey, O.AutoInc)
  def pregunta = column[String]("PREGUNTA")

  def * = (id, pregunta) <> (PreguntasAutovalidacion.tupled, PreguntasAutovalidacion.unapply)
}