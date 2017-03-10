package co.com.alianza.persistence.entities

import CustomDriver.simple._

case class ServicioComercial(id: Option[Int], url: String)

class ServicioComercialTable(tag: Tag) extends Table[ServicioComercial](tag, "SERVICIO_COMERCIAL") {

  def id = column[Option[Int]]("ID", O.PrimaryKey)
  def url = column[String]("URL")

  def * = (id, url) <> (ServicioComercial.tupled, ServicioComercial.unapply)
}

