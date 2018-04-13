package co.com.alianza.persistence.entities

import CustomDriver.simple._

case class PerfilLdap(idPerfilLdap: Int, ldapPerfil: String, perfilPortal: Int)

class PerfilLdapTable(tag: Tag) extends Table[PerfilLdap](tag, "PERFIL_LDAP") {
  def idPerfilLdap = column[Int]("ID_PERFIL_LDAP")
  def ldapPerfil = column[String]("LDAP_PERFIL")
  def perfilFk = column[Int]("PERFIL_FK")

  def * = (idPerfilLdap, ldapPerfil, perfilFk) <> (PerfilLdap.tupled, PerfilLdap.unapply)
}