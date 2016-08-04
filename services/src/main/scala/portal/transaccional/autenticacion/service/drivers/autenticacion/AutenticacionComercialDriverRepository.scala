package portal.transaccional.autenticacion.service.drivers.autenticacion

import javax.naming.NamingException

import co.com.alianza.commons.enumerations.UserTypesEnumeration
import co.com.alianza.util.ConfigReader
import org.joda.time.DateTime
import portal.transaccional.fiduciaria.autenticacion.storage.daos.ldap.AlianzaLdapDAO
import portal.transaccional.fiduciaria.autenticacion.storage.daos.portal.{AdministradoresDAO, UsuariosLoginDAO}

import scala.concurrent.{ExecutionContext, Future}
import slick.driver.JdbcProfile
import slick.jdbc.JdbcBackend._
import scalaz.Validation

case class AutenticacionComercialDriverRepository ( alianzaLdapDAO : AlianzaLdapDAO, adminDao : AdministradoresDAO ) extends AutenticacionComercialRepository {

  /**
   * Method that authenticates an user
   * @param userType Users type. (1 -> Fid, 2 -> Val)
   * @param username Users username
   * @param password Users password
   * @return A future with a Validation inside.
   *         On the right an AuthenticationSuccess class
   *         On the left a String indicating error to read
   */
  def authenticateLDAP( userType: Int, username: String, password: String, ip: String )( implicit ec: ExecutionContext, profile: JdbcProfile, db: Database ): Future[ Validation[ String, AuthenticationSuccess ] ] = {

    val organization: String = if ( userType == UserTypesEnumeration.fiduciaria.id ) "fiduciaria" else "valores"
    val idRoleDefault: Option[ Int ] = Some( 1 )
    val host: String = ConfigReader.readString( s"ldap.$organization.host" )
    val domain: String = ConfigReader.readString( s"ldap.$organization.domain" )
    val userName = username.toLowerCase
    val uLoginDao: UsuariosLoginDAO = new UsuariosLoginDAO()

    ( for {
      context <- alianzaLdapDAO.getLdapContext( host, domain, userName, password ) // Throws naming exception
      user <- alianzaLdapDAO.getUserInfo( userType, userName, context )
      uLogin <- uLoginDao.findByUsername( user.sAMAccountName )
      newUser = uLogin match {
        case Some( login ) => user.copy( idRole = login.idRole, last_login_ip = login.last_login_ip, last_login_date = login.last_login_date )
        case None          => user.copy( idRole = idRoleDefault )
      }
      token <- TokenManager.createToken( newUser, userType, domain )
      upLoginFields <- uLoginDao.insertOrUpdate( newUser, ip )
      session <- SessionManager.createSession( token, newUser )
    } yield {
      context.close()
      Validation.success( AuthenticationSuccess( token, false ) )
    } ) recover {
      case ex: NamingException => Validation.failure( "invalid-credentials" )
    }

  }

  /**
   * Method that authenticates an administrator
   * @param username Admin's username
   * @param password Admin's password
   * @param ip Request ip
   * @param ec Execution context for future manipulation (implicit)
   * @param profile Database driver (implicit)
   * @param db Database connection (implicit)
   * @return
   */
  def authenticateAdmin( username: String, password: String, ip: String )( implicit ec: ExecutionContext, profile: JdbcProfile, db: Database ): Future[ Validation[ String, AuthenticationSuccess ] ] = {

    val adminType: Int = UserTypesEnumeration.admin.id
    val userName = username.toLowerCase

    ( for {
      adminOp <- adminDao.findByUsername( userName ) if adminOp.isDefined // Throws NoSuchElementException
      validPassword <- Future( password.bcrypt.hash_=( adminOp.get.password ) ) if validPassword // Throws NoSuchElementException
      expiredPassword = checkExpirationDate( adminOp.get.recover_date ) // ExpiredPasswordException
      admin = adminFromPersistenceToDomain( adminOp.get )
      token <- TokenManager.createToken( admin, adminType, "" )
      upLoginFields <- adminDao.updateLastLoginFields( userName, ip, new DateTime() )
      session <- SessionManager.createSession( token, admin )
    } yield {
      Validation.success( AuthenticationSuccess( token, expiredPassword ) )
    } ) recover {
      case ex: NoSuchElementException   => Validation.failure( "invalid-credentials" )
      case ex: ExpiredPasswordException => Validation.failure( "expired-password" )
    }

  }


  /**
   * Checks if the password has expired
   * @param date
   * @param ec
   * @return
   */
  def checkExpirationDate( date: Option[ DateTime ] )( implicit ec: ExecutionContext ): Boolean = {
    date match {
      case Some( dateValue ) => {
        if ( dateValue.isBeforeNow ) throw new ExpiredPasswordException( "The password has expired" )
        else true
      }
      case None => false
    }
  }

}
