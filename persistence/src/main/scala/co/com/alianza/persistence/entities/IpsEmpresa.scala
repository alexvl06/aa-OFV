package co.com.alianza.persistence.entities

import CustomDriver.simple._

case class IpsEmpresa(idEmpresa: Int, ip: String)

/**
 * Created by david on 19/12/14.
 */
class IpsEmpresaTable(tag: Tag) extends Table[IpsEmpresa](tag, "IPS_EMPRESA") {

  def idEmpresa = column[Int]("ID_EMPRESA")

  def ip = column[String]("IP")

  // Every table needs a * projection with the same type as the table's type parameter
  def * = (idEmpresa, ip) <> (IpsEmpresa.tupled, IpsEmpresa.unapply)

}
