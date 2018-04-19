package co.com.alianza.persistence.entities

import CustomDriver.simple._

case class ValidacionPerfil(tipoValidacion: Int, idPerfil: Int, jerarquia: Int, parametro: String)

class ValidacionPerfilTable(tag: Tag) extends Table[ValidacionPerfil](tag, "TIPO_VALIDA_HAS_PERFIL") {

  def idTipoValidacion = column[Int]("TIPO_VALIDACION_FK")
  def idPerfil = column[Int]("PERFIL_FK")
  def jerarquia = column[Int]("JERARQUIA")
  def parametro = column[String]("PARAMETRO")

  def * = (idTipoValidacion, idPerfil, jerarquia, parametro) <> (ValidacionPerfil.tupled, ValidacionPerfil.unapply)

  def TipoValidacion = TableQuery[TipoValidacionTable]
  def Perfil = TableQuery[PerfilTable]

  lazy val tipoValidacionFk = foreignKey("tpo_va_h_per_per", idTipoValidacion, TipoValidacion)(e => e.id, onUpdate = ForeignKeyAction.NoAction, onDelete = ForeignKeyAction.NoAction)
  lazy val perfilFk = foreignKey("tpo_va_h_per_tpo_val", idPerfil, Perfil)(e => e.id, onUpdate = ForeignKeyAction.NoAction, onDelete = ForeignKeyAction.NoAction)
}

