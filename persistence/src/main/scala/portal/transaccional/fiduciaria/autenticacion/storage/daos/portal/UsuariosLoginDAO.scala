package portal.transaccional.fiduciaria.autenticacion.storage.daos.portal

import co.com.alianza.persistence.config.DBConfig
import co.com.alianza.persistence.entities.{User, UsuarioLogin, UsuariosLoginTable}
import co.com.alianza.persistence.util.mappers.JodatimeMapper
import org.joda.time.DateTime
import slick.driver.JdbcProfile
import slick.jdbc.JdbcBackend._
import slick.lifted.TableQuery

import scala.concurrent.Future

class UsuariosLoginDAO()(implicit val profile: JdbcProfile, val db: Database ) extends UsuariosLoginTable with DBConfig {

  /**
   * Table query references
   */
  val usuariosLogin: TableQuery[ UsuariosLogin ] = TableQuery[ UsuariosLogin ]

  /**
   * Method for table creation
   */
  def createSchema(): Future[ Unit ] = {
    db.run( usuariosLogin.schema.create )
  }

  /**
   * Method that drops table
   */
  def dropSchema(): Future[ Unit ] = {
    db.run( usuariosLogin.schema.drop )
  }

  /**
   * Verify if and User exist in Table usuarios_login and update his last login values,
   * otherwise insert the User in the Table
   * @param newUser User that will be insert or update
   * @param ip IP from the last login
   */
  def insertOrUpdate(newUser: User, ip: String ) = {
    newUser.last_login_ip match {
      case Some( userIp ) => updateLastLoginValues( newUser.sAMAccountName, ip, new DateTime() )
      case None           => insert( newUser, ip, new DateTime() )
    }
  }

  /**
   * Insert the User's username and the last login values
   * @param newUser User that will be insert or update
   * @param ip IP from the last login
   * @param date Date from the last login
   */
  def insert( newUser: User, ip: String, date: DateTime ): Future[ Int ] = {
    val query = usuariosLogin += UsuarioLogin( Some( newUser.sAMAccountName ), newUser.idRole, Some( ip ), Some( date ) )
    db.run( query )
  }

  /**
   * Update the User's last login values
   * @param newUser User that will be insert or update
   * @param ip IP from the last login
   * @param date Date from the last login
   */
  def updateLastLoginValues( newUser: String, ip: String, date: DateTime ): Future[ Int ] = {
    implicit val timeMapper: BaseColumnType[ DateTime ] = JodatimeMapper.mapper() // Mapper for datetime
    val query = for ( login <- usuariosLogin if login.username === newUser ) yield login.last_login_ip ~ login.last_login_date
    val updateAction = query.update( ( Some( ip ), Some( date ) ) )
    db.run( updateAction.transactionally )
  }

  /**
   * Query that returns a User by his username
   * @param username User's username
   * @return A Future with an Option of UserLogin Instance
   */
  def findByUsername( username: String ): Future[ Option[ UsuarioLogin ] ] = {
    val query = for ( uLogin <- usuariosLogin if uLogin.username === username ) yield uLogin
    val uLoginOption = query.result.headOption
    db.run( uLoginOption )
  }
}
