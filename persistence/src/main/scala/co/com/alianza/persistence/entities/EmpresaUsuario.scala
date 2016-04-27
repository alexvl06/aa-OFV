package co.com.alianza.persistence.entities

import CustomDriver.simple._

/**
 * Created by heco on 20/2/14.
 */

case class EmpresaUsuario(idEmpresa: Int, idUsuario: Int)

class EmpresaUsuarioTable(tag: Tag) extends Table[EmpresaUsuario](tag, "USUARIO_EMPRESARIAL_EMPRESA") {

  def idEmpresa = column[Int]("ID_EMPRESA")

  def idUsuario = column[Int]("ID_USUARIO_EMPRESARIAL")

  def * = (idEmpresa, idUsuario) <> (EmpresaUsuario.tupled, EmpresaUsuario.unapply)

}
