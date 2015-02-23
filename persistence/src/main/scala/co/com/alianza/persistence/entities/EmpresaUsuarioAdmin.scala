package co.com.alianza.persistence.entities

import CustomDriver.simple._

/**
 * Created by heco on 20/2/14.
 */

case class EmpresaUsuarioAdmin (idEmpresa: Int, idUsuario: Int)

class EmpresaUsuarioAdminTable (tag: Tag) extends Table[EmpresaUsuarioAdmin](tag, "USUARIO_EMPRESARIAL_ADMIN_EMPRESA") {

  def idEmpresa = column[Int]("ID_EMPRESA")

  def idUsuario = column[Int]("ID_USUARIO_EMPRESARIAL_ADMIN")

  def * = (idEmpresa, idUsuario) <> (EmpresaUsuarioAdmin.tupled, EmpresaUsuarioAdmin.unapply)

}
