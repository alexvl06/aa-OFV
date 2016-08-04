package co.com.alianza.persistence.entities

import co.com.alianza.persistence.config.DBConfig
import co.com.alianza.persistence.util.mappers.JodatimeMapper
import org.joda.time.DateTime

/**
 * Class that represents a User's login
 * @param username User's username
 * @param last_login_ip User's last login ip
 * @param last_login_date User's last login date
 */
case class UsuarioLogin( username: Option[ String ],
  idRole: Option[ Int ],
  last_login_ip: Option[ String ],
  last_login_date: Option[ DateTime ] )

/**
 * Trait that defines and map Table UsuarioLogin
 */
class UsuariosLoginTable extends RolesTable { this: DBConfig =>

  import profile.api._

  /**
   *  Table mapped for usuarios_login
   * @param tag Table tag
   */
  class UsuariosLogin( tag: Tag ) extends Table[ UsuarioLogin ]( tag, "usuarios_login" ) {

    implicit val timeMapper: BaseColumnType[ DateTime ] = JodatimeMapper.mapper()

    def username = column[ Option[ String ] ]( "username", O.PrimaryKey )
    def idRole = column[ Option[ Int ] ]( "idrole" )
    def last_login_ip = column[ Option[ String ] ]( "last_login_ip" )
    def last_login_date = column[ Option[ DateTime ] ]( "last_login_date" )

    def rolesFK = foreignKey( "fk_rol", idRole, roles )( _.id )

    override def * = ( username, idRole, last_login_ip, last_login_date ) <> ( ( UsuarioLogin.apply _ ).tupled, UsuarioLogin.unapply )
  }

}

