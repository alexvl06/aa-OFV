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
   * Method that validates an existing session
   * @param token Token to validate
   * @return A future with a Validation inside.
   */
  def validateToken( token: String )( implicit ec: ExecutionContext ): Future[ Validation[ String, TokenValidationSuccess ] ] = {

    ( for {
      valid <- TokenManager.validateToken( token ) if valid // Throws NoSuchElementException
      session <- SessionManager.querySession( token )
    } yield session match {
      case Found( id, genericUser ) => genericUser match {
        case us: User  => Validation.success( TokenValidationSuccess( Some( us ), None ) )
        case ad: Admin => Validation.success( TokenValidationSuccess( None, Some( ad ) ) )
      }
      case NotFound( id ) => Validation.failure( "invalid-credentials" )
    } ) recover {
      case ex: NoSuchElementException => Validation.failure( "invalid-token" )
    }

  }

  /**
   * Method that deletes an existent session
   * @param token Token to validate
   * @return A future with a Validation inside.
   */
  def deleteToken( token: String )( implicit ec: ExecutionContext ): Future[ Validation[ String, SessionDeleted ] ] = {

    ( for {
      valid <- TokenManager.validateToken( token ) if valid // Throws NoSuchElementException
      delete <- SessionManager.deleteSession( token )
    } yield delete match {
      case Deleted( id )    => Validation.success( SessionDeleted( deleted = true ) )
      case NotDeleted( id ) => Validation.success( SessionDeleted( deleted = false ) )
    } ) recover {
      case ex: NoSuchElementException => Validation.failure( "invalid-token" )
    }

  }

  /**
   * Method that verifies the recovering info and sends the password recovering email
   * @param value Recovering info value - email or username
   * @return a validation with with a PasswordRestores instance or a String
   */

  def recoverPassword( value: String )( implicit ec: ExecutionContext, profile: JdbcProfile, db: Database ): Future[ Validation[ String, PasswordRestored ] ] = {

    val adminDao: AdministradoresDAO = new AdministradoresDAO()

    val subject = ConfigReader.readString( "smtp.recover-subject" )

    val password = RandomString.randomAlphaNumericString( 6 )

    val maxRecoverDate = new DateTime().plusDays( 5 )

    ( for {
      update <- adminDao.findAndUpdateByRecoverInfo( value, password.bcrypt, maxRecoverDate ) if update > 0
      admin <- adminDao.findByRecoverInfo( value )
      adminD = adminFromPersistenceToDomain( admin.get )
      html = CustomMailMessage.createRecoveredPasswordMessage( adminD, password )
    } yield {
      Sender.send( adminD.email, subject, html ) // Doen't wait until send email is complete
      Validation.success( PasswordRestored( adminD.name ) )
    } ) recover {
      case ex: NoSuchElementException => Validation.failure( "invalid-process" )
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
