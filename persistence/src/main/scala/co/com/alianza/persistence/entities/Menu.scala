package co.com.alianza.persistence.entities

import CustomDriver.simple._
/**OFV LOGIN FASE 1**/
case class Menu(idMenu: Int, titulo: String, posicion: Int, isItem: String, url: Option[String], menuPadre: Option[Int], modulo: Int)

class MenuTable(tag: Tag) extends Table[Menu](tag, "MENU") {

  def idMenu = column[Int]("ID_MENU", O.PrimaryKey)
  def titulo = column[String]("TITULO")
  def posicion = column[Int]("POSICION")
  def isItem = column[String]("ES_ITEM")
  def url = column[Option[String]]("URL_MENU")
  def menuPadre = column[Option[Int]]("MENU_PADRE")
  def modulo = column[Int]("MODULO_MENU_FK")

  def ModuloMenu = TableQuery[ModuloMenuTable];

  lazy val moduloMenuFK = foreignKey("MODULO_MENU_FK", modulo, ModuloMenu)(e => e.idModuloempresarial, onUpdate = ForeignKeyAction.NoAction, onDelete = ForeignKeyAction.NoAction)

  def * = (idMenu, titulo, posicion, isItem, url, menuPadre, modulo) <> (Menu.tupled, Menu.unapply)
}