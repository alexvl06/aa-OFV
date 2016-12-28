package co.com.alianza.persistence.entities

import CustomDriver.simple._

case class Configuracion(llave: String, valor: String)

class ConfiguracionesTable(tag: Tag) extends Table[Configuracion](tag, "CONFIGURACIONES_ADMIN") {

  def llave = column[String]("LLAVE", O.PrimaryKey)

  def valor = column[String]("VALOR", O.PrimaryKey)

  // Every table needs a * projection with the same type as the table's type parameter
  def * = (llave, valor) <> (Configuracion.tupled, Configuracion.unapply)

}