package co.com.alianza.commons.enumerations

object RolesComercial extends Enumeration {

  case class RolesComercial(codigo: Int, nombre: String) extends Val(codigo, nombre)

  val Comercial = RolesComercial(1, "Comercial")
  val noComercial = RolesComercial(2, "No Comercial")

}
