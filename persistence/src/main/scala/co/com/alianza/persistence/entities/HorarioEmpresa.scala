package co.com.alianza.persistence.entities

import java.sql.Time
import CustomDriver.simple._

/**
 * @author hernando on 2/03/15.
 */
case class HorarioEmpresa(idEmpresa: Int, diaHabil: Boolean, sabado: Boolean, horaInicio: Time, horaFin: Time)

class HorarioEmpresaTable(tag: Tag) extends Table[HorarioEmpresa](tag, "HORARIO_EMPRESA") {

  def idEmpresa = column[Int]("ID_EMPRESA")

  def diaHabil = column[Boolean]("DIA_HABIL")

  def sabado = column[Boolean]("SABADO")

  def horaInicio = column[Time]("HORA_INICIO")

  def horaFin = column[Time]("HORA_FIN")

  // Every table needs a * projection with the same type as the table's type parameter
  def * = (idEmpresa, diaHabil, sabado, horaInicio, horaFin) <> (HorarioEmpresa.tupled, HorarioEmpresa.unapply)

}