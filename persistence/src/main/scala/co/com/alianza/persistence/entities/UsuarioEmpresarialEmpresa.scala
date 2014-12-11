package co.com.alianza.persistence.entities

import CustomDriver.simple._

/**
 * Created by manuel on 9/12/14.
 */
case class UsuarioEmpresarialEmpresa(idEmpresa: Int, idUsuarioEmpresarial: Int)

class UsuarioEmpresarialEmpresaTable(tag: Tag) extends Table[UsuarioEmpresarialEmpresa](tag, "USUARIO_EMPRESARIAL_EMPRESA") {

  def idEmpresa: Column[Int] = column[Int]("ID_EMPRESA")

  def idUsuarioEmpresarial: Column[Int] = column[Int]("ID_USUARIO_EMPRESARIAL")

  /** Primary key of DocumentosSiniestroSiniestros (database name documentos_siniestro_siniestros_pkey) */
  val pk = primaryKey("USUARIO_EMPRESARIAL_EMPRESA_PKEY", (idEmpresa, idUsuarioEmpresarial))

  def * =  (idEmpresa, idUsuarioEmpresarial) <> (UsuarioEmpresarialEmpresa.tupled, UsuarioEmpresarialEmpresa.unapply)

  val UsuariosEmpresariales = TableQuery[UsuarioEmpresarialTable]

  val Empresas = TableQuery[EmpresaTable]

  lazy val empresaFK = foreignKey("USUARIO_EMPRESARIAL_EMPRESA_EMPRESA_FK", idEmpresa, Empresas)(e => e.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)

  lazy val usuarioEmpresarialFK = foreignKey("USUARIO_EMPRESARIAL_EMPRESA_USUARIO_EMPRESARIAL_FK", idUsuarioEmpresarial, UsuariosEmpresariales)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)

}