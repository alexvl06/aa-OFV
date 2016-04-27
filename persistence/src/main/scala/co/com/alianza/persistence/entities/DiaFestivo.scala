package co.com.alianza.persistence.entities

import java.sql.Date
import CustomDriver.simple._

case class DiaFestivo(fecha: Option[Date], descripcion: Option[String])

class DiaFestivoTable(tag: Tag) extends Table[DiaFestivo](tag, "DIA_FESTIVO") {
  def fecha = column[Option[Date]]("FECHA") // This is the primary key column
  def descripcion = column[Option[String]]("DESCRIPCION")
  // Every table needs a * projection with the same type as the table's type parameter
  def * = (fecha, descripcion) <> (DiaFestivo.tupled, DiaFestivo.unapply)
}
