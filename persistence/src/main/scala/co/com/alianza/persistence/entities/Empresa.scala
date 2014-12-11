package co.com.alianza.persistence.entities

import CustomDriver.simple._

/**
 * Created by manuel on 10/12/14.
 */

case class Empresa (id: Int, nit: String)

class EmpresaTable (tag: Tag) extends Table[Empresa](tag, "EMPRESA") {

  def id = column[Int]("ID")

  def nit = column[String]("NIT")

  def * = (id, nit) <> (Empresa.tupled, Empresa.unapply)

}
