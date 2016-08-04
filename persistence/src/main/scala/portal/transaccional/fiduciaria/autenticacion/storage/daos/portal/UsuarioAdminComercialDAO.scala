package portal.transaccional.fiduciaria.autenticacion.storage.daos.portal

import co.com.alianza.persistence.config.DBConfig
import co.com.alianza.persistence.entities.{UsuarioAdminComercial, UsuarioAdminComercialTable}
import co.com.alianza.persistence.util.mappers.JodatimeMapper
import org.joda.time.DateTime
import slick.driver.JdbcProfile
import slick.jdbc.JdbcBackend._
import slick.lifted.TableQuery

import scala.concurrent.Future

class UsuarioAdminComercialDAO()(implicit dBConfig: DBConfig) extends UsuarioAdminComercialTable with UsuarioAdminComercialDAOs {

  import dBConfig.profile.api._

  /**
   * Table query reference
   */
  val usuarioAdminComercial: TableQuery[ UsuarioAdminComercial ] = TableQuery[ UsuarioAdminComercial ]

  /**
   * Method thant inserts a new administrador instance
 *
   * @param admin Instance to insert
   * @return A future of an Option with the inserted instance id
   */
  def insert( admin: UsuarioAdminComercial ): Future[ Option[ Int ] ] = {
    val query = ( usuarioAdminComercial returning usuarioAdminComercial.map( _.id ) ) += admin
    dBConfig.db.run( query )
  }

  /**
   * Method that updates an Admin's password
 *
   * @param username Admin's username
   * @param newPassword Admin's new password
   * @return A future with the updated rows
   */
  def updatePassword( username: String, newPassword: String ): Future[ Int ] = {
    implicit val timeMapper: BaseColumnType[ DateTime ] = JodatimeMapper.mapper() // Mapper for datetime
    val query = for (admin <- usuarioAdminComercial if admin.username === username ) yield admin.password ~ admin.recover_date
    val update = query.update( ( newPassword, None ) )
    dBConfig.db.run( update.transactionally )
  }

  /**
   * Method that finds an administrator by its username
 *
   * @param username Admin's username
   * @return A future with an option of Administrator instance
   */
  def findByUsername( username: String ): Future[ Option[ UsuarioAdminComercial ] ] = {
    val query = for (admin <- usuarioAdminComercial if admin.username === username ) yield admin
    val adminOption = query.result.headOption
    dBConfig.db.run( adminOption )
  }

  /**
   * Method that updates an administrator's last login fields
 *
   * @param username Admin's username
   * @param ip New login ip
   * @param date New login date
   * @return A future with an int
   */
  def updateLastLoginFields( username: String, ip: String, date: DateTime ): Future[ Int ] = {
    implicit val timeMapper: BaseColumnType[ DateTime ] = JodatimeMapper.mapper() // Mapper for datetime
    val query = for (admin <- usuarioAdminComercial if admin.username === username ) yield admin.last_login_ip ~ admin.last_login_date
    val updateAction = query.update( ( Some( ip ), Some( date ) ) )
    dBConfig.db.run( updateAction.transactionally )
  }

  def findAndUpdateByRecoverInfo( info: String, password: String, date: DateTime ): Future[ Int ] = {
    implicit val timeMapper: BaseColumnType[ DateTime ] = JodatimeMapper.mapper()
    val queryForUpdate = for (admin <- usuarioAdminComercial if admin.username === info || admin.email === info ) yield admin.password ~ admin.recover_date
    val updateAction = queryForUpdate.update( ( password, Some( date ) ) )
    val query = for (admin <- usuarioAdminComercial if admin.username === info || admin.email === info ) yield admin
    dBConfig.db.run( updateAction.transactionally )
  }

  def findByRecoverInfo( info: String ): Future[ Option[ UsuarioAdminComercial ] ] = {
    val query = for (admin <- usuarioAdminComercial if admin.username === info || admin.email === info ) yield admin
    val adminOption = query.result.headOption
    dBConfig.db.run( adminOption )
  }

}
