package portal.transaccional.fiduciaria.autenticacion.storage.daos.ldap

import java.util
import javax.naming.directory.{Attributes, SearchControls, SearchResult}
import javax.naming.ldap.{InitialLdapContext, LdapContext}
import javax.naming.{Context, NamingEnumeration}

import co.com.alianza.commons.enumerations.TiposCliente
import co.com.alianza.persistence.dto.UsuarioLdapDTO

import scala.concurrent.{ExecutionContext, Future}

case class AlianzaLdapDAO() extends AlianzaLdapDAOs {

  /**
   * Method that retrieves ldap context
   * @param username Username
   * @param password Password
   * @return A future with LDAPContext
   *         Throws javax.naming.NamingException if authentication failed
   */
  def getLdapContext(host: String, domain: String,
    username: String, password: String)(implicit executionContext: ExecutionContext): Future[LdapContext] = Future {

    // CONNECTION EN  ENVIRONMENT
    val environment = new util.Hashtable[String, String]()
    environment.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory")
    environment.put(Context.PROVIDER_URL, host)
    environment.put(Context.SECURITY_AUTHENTICATION, "simple")
    environment.put(Context.SECURITY_PRINCIPAL, s"$username@$domain")
    environment.put(Context.SECURITY_CREDENTIALS, s"$password")

    // Context
    new InitialLdapContext(environment, null)

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

        Some(UsuarioLdapDTO(user, Some(sat), Some(dn), Some(sn), Some(gn), None, Some(upn), None, None, None))

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
