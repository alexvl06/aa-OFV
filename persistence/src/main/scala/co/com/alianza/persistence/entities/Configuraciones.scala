package co.com.alianza.persistence.entities

import CustomDriver.simple._

case class Configuraciones(llave: String, valor: String)

class ConfiguracionesTable(tag: Tag) extends Table[Configuraciones](tag, "CONFIGURACIONES_ADMIN") {

  def llave = column[String]("LLAVE", O.PrimaryKey)

  def valor = column[String]("VALOR", O.PrimaryKey)

  // Every table needs a * projection with the same type as the table's type parameter
  def * = (llave, valor) <>(Configuraciones.tupled, Configuraciones.unapply)

}