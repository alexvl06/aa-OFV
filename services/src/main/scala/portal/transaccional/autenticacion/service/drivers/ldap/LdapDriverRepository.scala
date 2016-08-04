package portal.transaccional.autenticacion.service.drivers.ldap

import co.com.alianza.commons.enumerations.TiposCliente
import co.com.alianza.exceptions.ValidacionException
import co.com.alianza.persistence.dto.UsuarioLdapDTO
import co.com.alianza.util.ConfigReader
import portal.transaccional.fiduciaria.autenticacion.storage.daos.ldap.AlianzaLdapDAOs

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by dfbaratov on 4/08/16.
  */
case class LdapDriverRepository(alianzaLdapDAO: AlianzaLdapDAOs)(implicit val ex: ExecutionContext) extends LdapRepository {

   def autenticarLdap(userType: Int, username: String, password: String): Future[Boolean] = {
     val organization: String = if ( userType == TiposCliente.comercialFiduciaria.id ) "fiduciaria" else "valores"
     val idRoleDefault: Option[ Int ] = Some( 1 )
     val host: String = ConfigReader.readString( s"ldap.$organization.host" )
     val domain: String = ConfigReader.readString( s"ldap.$organization.domain" )
     val userName = username.toLowerCase
//     val uLoginDao: UsuarioComercialDAO = new UsuarioComercialDAO()

     for {
       context <- alianzaLdapDAO.getLdapContext(host, domain, userName, password) // Throws naming exception
       user <- alianzaLdapDAO.getUserInfo(userType, userName, context)
       respuesta <- validarRespuestaLdap(user)
     } yield respuesta


   }

  private def validarRespuestaLdap(user: Option[UsuarioLdapDTO]): Future[Boolean] = {
    user match {
      case Some(user) => Future.successful(true)
      case None => Future.failed(ValidacionException("401.2", "Error Cliente Alianza"))
    }
  }

}
