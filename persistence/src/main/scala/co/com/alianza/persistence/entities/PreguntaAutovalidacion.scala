package co.com.alianza.persistence.entities

import CustomDriver.simple._

/**
 *
 * @author seven4n
 */

case class PreguntaAutovalidacion(id: Int, pregunta: String)

class PreguntasAutovalidacionTable(tag: Tag) extends Table[PreguntaAutovalidacion](tag, "PREGUNTAS_AUTOVALIDACION") {
  def id = column[Int]("ID", O.PrimaryKey, O.AutoInc)
  def pregunta = column[String]("PREGUNTA")

  def * = (id, pregunta) <> (PreguntaAutovalidacion.tupled, PreguntaAutovalidacion.unapply)
}