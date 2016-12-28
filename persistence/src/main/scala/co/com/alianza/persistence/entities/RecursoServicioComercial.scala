package co.com.alianza.persistence.entities

import CustomDriver.simple._

case class RecursoServicioComercial(idRecurso: Option[Int], idServicio: Option[Int])

class RecursoServicioComercialTable(tag: Tag) extends Table[RecursoServicioComercial](tag, "RECURSO_SERVICIO_COMERCIAL") {

  def idRecurso = column[Option[Int]]("ID_RECURSO", O.PrimaryKey)
  def idServicio = column[Option[Int]]("ID_SERVICIO", O.PrimaryKey)

  def * = (idRecurso, idServicio) <> (RecursoServicioComercial.tupled, RecursoServicioComercial.unapply)
}

