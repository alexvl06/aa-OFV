package co.com.alianza.persistence.entities

import co.com.alianza.persistence.entities.CustomDriver.simple._

/**
 * Created by david on 19/12/14.
 */
class IpsUsuarioEmpresarialTable(tag: Tag) extends Table[IpsUsuario](tag, "IPS_USUARIO_EMPRESARIAL") {

  def idUsuario = column[Int]("ID_USUARIO")

  def ip = column[String]("IP")

  // Every table needs a * projection with the same type as the table's type parameter
  def * = (idUsuario, ip) <> (IpsUsuario.tupled, IpsUsuario.unapply)

}
