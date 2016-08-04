package portal.transaccional.fiduciaria.autenticacion.storage.daos.portal

import co.com.alianza.persistence.config.DBConfig
import co.com.alianza.persistence.entities.{Administrador, AdministradoresTable}
import co.com.alianza.persistence.util.mappers.JodatimeMapper
import org.joda.time.DateTime
import slick.driver.JdbcProfile
import slick.jdbc.JdbcBackend._

import scala.concurrent.Future

class AdministradoresDAO()(implicit val profile: JdbcProfile, val db: Database ) extends AdministradoresTable with DBConfig {

  import profile.api._

  /**
   * Table query reference
   */
  val administradores: TableQuery[ Administradores ] = TableQuery[ Administradores ]

  /**
   * Method thant inserts a new administrador instance
   * @param admin Instance to insert
   * @return A future of an Option with the inserted instance id
   */
  def insert( admin: Administrador ): Future[ Option[ Int ] ] = {
    val query = ( administradores returning administradores.map( _.id ) ) += admin
    db.run( query )
  }

  /**
   * Method that updates an Admin's password
   * @param username Admin's username
   * @param newPassword Admin's new password
   * @return A future with the updated rows
   */
  def updatePassword( username: String, newPassword: String ): Future[ Int ] = {
    implicit val timeMapper: BaseColumnType[ DateTime ] = JodatimeMapper.mapper() // Mapper for datetime
    val query = for ( admin <- administradores if admin.username === username ) yield admin.password ~ admin.recover_date
    val update = query.update( ( newPassword, None ) )
    db.run( update.transactionally )
  }

  /**
   * Method that finds an administrator by its username
   * @param username Admin's username
   * @return A future with an option of Administrator instance
   */
  def findByUsername( username: String ): Future[ Option[ Administrador ] ] = {
    val query = for ( admin <- administradores if admin.username === username ) yield admin
    val adminOption = query.result.headOption
    db.run( adminOption )
  }

  /**
   * Method that updates an administrator's last login fields
   * @param username Admin's username
   * @param ip New login ip
   * @param date New login date
   * @return A future with an int
   */
  def updateLastLoginFields( username: String, ip: String, date: DateTime ): Future[ Int ] = {
    implicit val timeMapper: BaseColumnType[ DateTime ] = JodatimeMapper.mapper() // Mapper for datetime
    val query = for ( admin <- administradores if admin.username === username ) yield admin.last_login_ip ~ admin.last_login_date
    val updateAction = query.update( ( Some( ip ), Some( date ) ) )
    db.run( updateAction.transactionally )
  }

  def findAndUpdateByRecoverInfo( info: String, password: String, date: DateTime ): Future[ Int ] = {
    implicit val timeMapper: BaseColumnType[ DateTime ] = JodatimeMapper.mapper()
    val queryForUpdate = for ( admin <- administradores if admin.username === info || admin.email === info ) yield admin.password ~ admin.recover_date
    val updateAction = queryForUpdate.update( ( password, Some( date ) ) )
    val query = for ( admin <- administradores if admin.username === info || admin.email === info ) yield admin
    db.run( updateAction.transactionally )
  }

  def findByRecoverInfo( info: String ): Future[ Option[ Administrador ] ] = {
    val query = for ( admin <- administradores if admin.username === info || admin.email === info ) yield admin
    val adminOption = query.result.headOption
    db.run( adminOption )
  }

}
