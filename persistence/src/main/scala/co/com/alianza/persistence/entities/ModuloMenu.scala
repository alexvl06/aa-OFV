package co.com.alianza.persistence.entities

import CustomDriver.simple._

case class ModuloMenu(idModuloempresarial: Int, modulo: String)

class ModuloMenuTable(tag: Tag) extends Table[ModuloMenu](tag, "MODULO_MENU") {

  def idModuloempresarial = column[Int]("ID_MODULO_MENU", O.PrimaryKey)
  def modulo = column[String]("DESC_MODULO_MENU")

  def * = (idModuloempresarial, modulo) <> (ModuloMenu.tupled, ModuloMenu.unapply)
}
