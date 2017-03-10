package portal.transaccional.autenticacion.service.drivers.ldap

import co.com.alianza.commons.enumerations.TiposCliente._
import co.com.alianza.persistence.dto.UsuarioLdapDTO

import scala.concurrent.Future

/**
 * Created by dfbaratov on 4/08/16.
 */
trait LdapRepository {

  def autenticarLdap(usuario: String, tipoCliente: TiposCliente, password: String): Future[UsuarioLdapDTO]

  def validarSACLdap(usuario: UsuarioLdapDTO, esSAC: Boolean): Future[Boolean]

}
