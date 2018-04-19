package co.com.alianza.persistence.entities

import CustomDriver.simple._

case class PerfilMenu(idPerfil: Int, idMenu: Int)

class PerfilMenuTable(tag: Tag) extends Table[PerfilMenu](tag, "PERFIL_MENU") {
  def idPerfil = column[Int]("PERFIL_FK")
  def idMenu = column[Int]("MENU_FK")

  def pk = primaryKey("perfil_menu_pk", (idPerfil, idMenu))

  def * = (idPerfil, idMenu) <> (PerfilMenu.tupled, PerfilMenu.unapply)

  val Perfil = TableQuery[PerfilTable]
  val Menu = TableQuery[MenuTable]

  lazy val perfilFK = foreignKey("per_men_menu", idPerfil, Perfil)(e => e.id, onUpdate = ForeignKeyAction.NoAction, onDelete = ForeignKeyAction.NoAction)

  lazy val menuFk = foreignKey("per_men_per", idMenu, Menu)(r => r.idMenu, onUpdate = ForeignKeyAction.NoAction, onDelete = ForeignKeyAction.NoAction)
}