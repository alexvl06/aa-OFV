package enumerations

import enumerations.ConfiguracionEnum.nextId

object TipoIngresoUsuario extends Enumeration {
  case class Val(idIngreso: Int, name: String) extends super.Val(nextId, name)
  val BD_PORTAL = Val(1, "BD Portal")
  val LDAP_ALIANZA = Val(2, "LDAP Alianza")
  val LDAP_VALORES = Val(3, "LDAP Valores")
}
