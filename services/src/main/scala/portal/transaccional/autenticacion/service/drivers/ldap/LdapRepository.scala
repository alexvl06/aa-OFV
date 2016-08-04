package portal.transaccional.autenticacion.service.drivers.ldap

import scala.concurrent.Future

/**
  * Created by dfbaratov on 4/08/16.
  */
trait LdapRepository {

  def autenticarLdap( userType: Int, username: String, password: String) : Future[Boolean]

}
