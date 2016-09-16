package portal.transaccional.autenticacion.service.drivers.ldap

import javax.naming.ldap.LdapContext

import co.com.alianza.commons.enumerations.TiposCliente._
import co.com.alianza.exceptions.ValidacionException
import co.com.alianza.persistence.dto.UsuarioLdapDTO
import portal.transaccional.fiduciaria.autenticacion.storage.daos.ldap.AlianzaLdapDAOs

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.Try

/**
 * Created by dfbaratov on 2016
 */
case class LdapDriverRepository(alianzaLdapDAO: AlianzaLdapDAOs)(implicit val ex: ExecutionContext) extends LdapRepository {

  def autenticarLdap(usuario: String, tipoCliente: TiposCliente, password: String): Future[UsuarioLdapDTO] = {
    val idRoleDefault: Option[Int] = Some(1)
    val userName = usuario.toLowerCase
    val user = for {
      context <- obtenerContexto(usuario, tipoCliente, password) // Throws naming exception
      usuarioOption <- alianzaLdapDAO.getUserInfo(tipoCliente, userName, context)
      respuesta <- validarRespuestaLdap(usuarioOption)

    } yield respuesta
    user
  }

  //TODO: Realizar la validacion del campo 'sac'
  def validarSACLdap(usuario: UsuarioLdapDTO, esSAC: Boolean = false): Future[Boolean] = {
    Future(true)
  }

  private def obtenerContexto(usuario: String, tipoCliente: TiposCliente, password: String): Future[LdapContext] = {
    val contextTry = Try {
      alianzaLdapDAO.getLdapContext(usuario, password, tipoCliente)
    }

    if (contextTry.isSuccess) {
      Future.successful(contextTry.get)
    } else {
      //TODO: Encontrar una mejor manera de manejar las excepciones
      Future.failed(new ValidacionException("401.1", "Credenciales invalidas"))
    }
  }

  private def validarRespuestaLdap(usuarioOption: Option[UsuarioLdapDTO]): Future[UsuarioLdapDTO] = {
    usuarioOption match {
      case Some(usuario) => Future.successful(usuario)
      case None => Future.failed(ValidacionException("401.2", "Error Cliente Alianza"))
    }
  }

}
