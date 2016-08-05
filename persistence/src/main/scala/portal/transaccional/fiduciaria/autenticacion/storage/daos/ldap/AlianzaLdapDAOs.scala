package portal.transaccional.fiduciaria.autenticacion.storage.daos.ldap

import javax.naming.ldap.LdapContext

import co.com.alianza.persistence.dto.UsuarioLdapDTO

import scala.concurrent.{ ExecutionContext, Future }

trait AlianzaLdapDAOs {

  def getLdapContext(username: String, password: String, tipoUsuario: Int)(implicit executionContext: ExecutionContext): LdapContext

  def getUserInfo(userType: Int, user: String, ctx: LdapContext)(implicit executionContext: ExecutionContext): Future[Option[UsuarioLdapDTO]]

}
