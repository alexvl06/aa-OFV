package co.com.alianza.persistence.entities

import java.sql.Timestamp
import CustomDriver.simple._

/**
 * Created by manuel on 18/12/14.
 */
case class UsuarioEmpresarialAdminEmpresa(idEmpresa: Int, idUsuarioEmpresarialAdmin: Int)

class UsuarioEmpresarialAdminEmpresaTable(tag: Tag) extends Table[UsuarioEmpresarialAdminEmpresa](tag, "USUARIO_EMPRESARIAL_ADMIN_EMPRESA") {

  def idEmpresa = column[Int]("ID_EMPRESA")

  def idUsuarioEmpresarialAdmin = column[Int]("ID_USUARIO_EMPRESARIAL_ADMIN")

  /** Primary key of DocumentosSiniestroSiniestros (database name documentos_siniestro_siniestros_pkey) */
  val pk = primaryKey("PK_USUARIO_EMPRESARIAL_ADMIN_EMPRESA", (idEmpresa, idUsuarioEmpresarialAdmin))

  def * = (idEmpresa, idUsuarioEmpresarialAdmin) <> (UsuarioEmpresarialAdminEmpresa.tupled, UsuarioEmpresarialAdminEmpresa.unapply)

  val UsuariosEmpresarialesAdmin = TableQuery[UsuarioEmpresarialAdminTable]

  val Empresas = TableQuery[EmpresaTable]

  lazy val empresaFK = foreignKey("USUARIO_EMPRESARIAL_ADMIN_EMPRESA_ID_EMPRESA_fkey", idEmpresa, Empresas)(e => e.id, onUpdate = ForeignKeyAction.NoAction, onDelete = ForeignKeyAction.NoAction)

  lazy val usuarioEmpresarialFK = foreignKey("USUARIO_EMPRESARIAL_ADMIN_EMPRESA_ID_USUARIO_EMPRESARIAL_ADMIN_FK", idUsuarioEmpresarialAdmin, UsuariosEmpresarialesAdmin)(r => r.id, onUpdate = ForeignKeyAction.NoAction, onDelete = ForeignKeyAction.NoAction)

}