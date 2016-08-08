package portal.transaccional.autenticacion.service.drivers.ldap

import co.com.alianza.persistence.dto.UsuarioLdapDTO

import scala.concurrent.Future

/**
 * Created by dfbaratov on 4/08/16.
 */
trait LdapRepository {

  def autenticarLdap(usuario: String, tipoUsuario: Int, password: String): Future[UsuarioLdapDTO]

}
