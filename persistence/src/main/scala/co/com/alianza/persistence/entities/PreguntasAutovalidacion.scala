package co.com.alianza.persistence.entities

import CustomDriver.simple._

/**
 *
 * @author seven4n
 */

case class PreguntasAutovalidacion(id: Option[Int], pregunta: String)


class PreguntasAutovalidacionTable(tag: Tag) extends Table[PreguntasAutovalidacion](tag, "PREGUNTAS_AUTOVALIDACION") {
  def id      = column[Option[Int]]("ID", O.PrimaryKey, O.AutoInc)
  def pregunta  = column[String]("PREGUNTA")

  def * =  (id, pregunta) <> (PreguntasAutovalidacion.tupled, PreguntasAutovalidacion.unapply)
}