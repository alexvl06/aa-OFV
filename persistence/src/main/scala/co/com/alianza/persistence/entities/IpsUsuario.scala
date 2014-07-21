package co.com.alianza.persistence.entities

import CustomDriver.simple._

/**
 * Created by david on 12/06/14.
 */

case class IpsUsuario(idUsuario: Int, ip: String)

class IpsUsuarioTable(tag: Tag) extends Table[IpsUsuario](tag, "IPS_USUARIO") {

  def idUsuario = column[Int]("ID_USUARIO")

  def ip = column[String]("IP")

  // Every table needs a * projection with the same type as the table's type parameter
  def * = (idUsuario, ip) <> (IpsUsuario.tupled, IpsUsuario.unapply)
}
