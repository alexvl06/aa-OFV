package co.com.alianza.persistence.entities

import co.com.alianza.persistence.config.DBConfig
import co.com.alianza.persistence.util.mappers.JodatimeMapper
import org.joda.time.DateTime

/**
 * Class that represents an Administrador instance
 * @param id Administrador id
 * @param username Administrador username
 * @param password Administrador password
 * @param name Administrador name
 * @param email Administrador email
 * @param last_login_ip Administrador last login ip
 * @param last_login_date Administrador last login date
 */
case class Administrador( id: Option[ Int ],
  username: String,
  password: String,
  name: String,
  email: String,
  last_login_ip: Option[ String ],
  last_login_date: Option[ DateTime ],
  recover_date: Option[ DateTime ] )

/**
 * Trait that defines and map table administradores
 */
trait AdministradoresTable { this: DBConfig =>

  import profile.api._

  /**
   * Table mapping for 'administradores'
   * @param tag Table Tag
   */
  class Administradores( tag: Tag ) extends Table[ Administrador ]( tag, "administradores" ) {

    implicit val timeMapper: BaseColumnType[ DateTime ] = JodatimeMapper.mapper()

    def id = column[ Option[ Int ] ]( "id", O.PrimaryKey, O.AutoInc )
    def username = column[ String ]( "username" )
    def password = column[ String ]( "password" )
    def name = column[ String ]( "name" )
    def email = column[ String ]( "email" )
    def last_login_ip = column[ Option[ String ] ]( "last_login_ip" )
    def last_login_date = column[ Option[ DateTime ] ]( "last_login_date" )
    def recover_date = column[ Option[ DateTime ] ]( "recover_date" )

    def * = ( id, username, password, name, email, last_login_ip, last_login_date, recover_date ) <> ( ( Administrador.apply _ ).tupled, Administrador.unapply )
  }

}
