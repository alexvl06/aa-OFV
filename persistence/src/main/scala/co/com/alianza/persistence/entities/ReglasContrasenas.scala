package co.com.alianza.persistence.entities

import CustomDriver.simple._

/**
 * Created by david on 12/06/14.
 */

case class ReglasContrasenas (llave: String, valor: String)

class ReglasContrasenasTable(tag: Tag) extends Table[ReglasContrasenas](tag, "REGLAS_CONTRASENAS") {

  def llave = column[String]("LLAVE", O.PrimaryKey)

  def valor = column[String]("VALOR", O.PrimaryKey)

  // Every table needs a * projection with the same type as the table's type parameter
  def * = (llave, valor) <> (ReglasContrasenas.tupled, ReglasContrasenas.unapply)
}
