package portal.transaccional.fiduciaria.autenticacion.storage.daos.ldap

import java.util
import javax.naming.directory.{ Attributes, SearchControls, SearchResult }
import javax.naming.ldap.{ InitialLdapContext, LdapContext }
import javax.naming.{ Context, NamingEnumeration }

import co.com.alianza.commons.enumerations.TiposCliente
import co.com.alianza.exceptions.ValidacionException
import co.com.alianza.persistence.dto.UsuarioLdapDTO
import co.com.alianza.util.ConfigApp
import com.typesafe.config.Config

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.Try

case class AlianzaLdapDAO() extends AlianzaLdapDAOs {

  /**
   * Method that retrieves ldap context
   * @param username Username
   * @param password Password
   * @return A future with LDAPContext
   *         Throws javax.naming.NamingException if authentication failed
   */
  def getLdapContext(username: String, password: String, tipoUsuario: Int)(implicit executionContext: ExecutionContext): Future[LdapContext] = {
    implicit val conf: Config = ConfigApp.conf
    val organization: String = if (tipoUsuario == TiposCliente.comercialFiduciaria.id) "fiduciaria" else "valores"
    val host: String = conf.getString(s"ldap.$organization.host")
    val domain: String = conf.getString(s"ldap.$organization.domain")
    // CONNECTION EN  ENVIRONMENT
    val environment = new util.Hashtable[String, String]()
    environment.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory")
    environment.put(Context.PROVIDER_URL, host)
    environment.put(Context.SECURITY_AUTHENTICATION, "simple")
    environment.put(Context.SECURITY_PRINCIPAL, s"$username@$domain")
    environment.put(Context.SECURITY_CREDENTIALS, s"$password")
    // Context
    val contextTry = Try {
      new InitialLdapContext(environment, null)
    }
    if (contextTry.isFailure) Future.failed(new ValidacionException("500", "error conexion ldap"))
    else Future.successful(contextTry.get)
  }

  /**
   * Method that search an user in LDAP context
   * @param user Username
   * @param ctx LDAP context
   * @return A future with an user
   */
  def getUserInfo(userType: Int, user: String, ctx: LdapContext)(implicit executionContext: ExecutionContext): Future[Option[UsuarioLdapDTO]] = Future {
    // SEARCH FILTER
    val filter: String = s"(&(&(objectClass=person)(objectCategory=user))(sAMAccountName=$user))"
    // QUERY
    val searchContext = if (userType == TiposCliente.comercialFiduciaria.id) "DC=Alianza,DC=com,DC=co" else "DC=alianzavaloresint,DC=com"
    val search: NamingEnumeration[SearchResult] = ctx.search(searchContext, filter, getSearchControls)
    // USER INSTANCE
    val userInstance: Option[UsuarioLdapDTO] = search.hasMore match {
      case true =>
        val attrs: Attributes = search.next().getAttributes
        val dn = attrs.get("distinguishedName").get.toString
        val sn = attrs.get("sn").get.toString
        val gn = attrs.get("givenname").get.toString
        //        val mof = attrs.get( "memberOf" ).get.toString
        val upn = attrs.get("userPrincipalName").get.toString
        val sat = attrs.get("sAMAccountType").get.toString
        Some(UsuarioLdapDTO(user, Some(sat), Some(dn), Some(sn), Some(gn), None, upn, None, None, None))

      case false => None
    }
    userInstance
  }

  /**
   * Method that retrieves ldap search controls
   * @return A future with LDAP search controls
   */
  private def getSearchControls: SearchControls = {
    // SEARCH ATTRIBUTES
    val attrIDs: Array[String] = Array(
      "distinguishedName",
      "sn",
      "givenname",
      "mail",
      "member",
      //      "memberOf",
      "userPrincipalName",
      "sAMAccountType"
    )
    // SEARCH CONTROLS
    val sc = new SearchControls()
    sc.setSearchScope(SearchControls.SUBTREE_SCOPE)
    sc.setReturningAttributes(attrIDs)
    sc
  }

}
