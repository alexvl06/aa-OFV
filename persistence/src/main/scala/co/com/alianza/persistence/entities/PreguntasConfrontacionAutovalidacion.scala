package co.com.alianza.persistence.entities

import CustomDriver.simple._

/**
 *
 * @author seven4n
 */

case class PreguntasConfrontacionAutovalidacion(id: Option[Int], pregunta: String)


class PreguntasConfrontacionAutovalidacionTable(tag: Tag) extends Table[PreguntasConfrontacionAutovalidacion](tag, "PREGUNTAS_CONFRONTACION_AUTOVALIDACION") {
  def id      = column[Option[Int]]("ID", O.PrimaryKey, O.AutoInc)
  def pregunta  = column[String]("PREGUNTA")

  def * =  (id, pregunta) <> (PreguntasConfrontacionAutovalidacion.tupled, PreguntasConfrontacionAutovalidacion.unapply)
}