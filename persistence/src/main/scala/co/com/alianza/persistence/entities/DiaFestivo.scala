package co.com.alianza.persistence.entities

import java.sql.Date
import CustomDriver.simple._

case class DiaFestivo(fecha: Date, descripcion: String)

class DiaFestivoTable(tag: Tag) extends Table[DiaFestivo](tag, "DIA_FESTIVO") {

  def fecha = column[Date]("FECHA") // This is the primary key column

  def descripcion = column[String]("DESCRIPCION")
  // Every table needs a * projection with the same type as the table's type parameter
  def * = (fecha, descripcion) <> (DiaFestivo.tupled, DiaFestivo.unapply)
}
