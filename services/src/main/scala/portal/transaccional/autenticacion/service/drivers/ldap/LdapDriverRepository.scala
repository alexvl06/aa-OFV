package portal.transaccional.autenticacion.service.drivers.ldap

import co.com.alianza.exceptions.ValidacionException
import co.com.alianza.persistence.dto.UsuarioLdapDTO
import portal.transaccional.fiduciaria.autenticacion.storage.daos.ldap.AlianzaLdapDAOs

import scala.concurrent.{ ExecutionContext, Future }

/**
 * Created by dfbaratov on 4/08/16.
 */
case class LdapDriverRepository(alianzaLdapDAO: AlianzaLdapDAOs)(implicit val ex: ExecutionContext) extends LdapRepository {

  def autenticarLdap(usuario: String, tipoUsuario: Int, password: String): Future[UsuarioLdapDTO] = {
    val idRoleDefault: Option[Int] = Some(1)
    val userName = usuario.toLowerCase
    for {
      context <- alianzaLdapDAO.getLdapContext(userName, password, tipoUsuario) // Throws naming exception
      usuarioOption <- alianzaLdapDAO.getUserInfo(tipoUsuario, userName, context)
      respuesta <- validarRespuestaLdap(usuarioOption)
    } yield respuesta
  }

  private def validarRespuestaLdap(usuarioOption: Option[UsuarioLdapDTO]): Future[UsuarioLdapDTO] = {
    usuarioOption match {
      case Some(usuario) => Future.successful(usuario)
      case None => Future.failed(ValidacionException("401.2", "Error Cliente Alianza"))
    }
  }

}
