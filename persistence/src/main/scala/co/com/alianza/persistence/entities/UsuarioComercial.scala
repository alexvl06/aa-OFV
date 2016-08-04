package co.com.alianza.persistence.entities

import java.sql.Timestamp

import co.com.alianza.persistence.config.DBConfig
import co.com.alianza.persistence.util.mappers.JodatimeMapper
import org.joda.time.DateTime
import slick.profile.RelationalTableComponent.Table

case class UsuarioComercial(username: Option[ String ],
                            idRole: Option[ Int ],
                            last_login_ip: Option[ String ],
                            last_login_date: Option[ DateTime ] )

class UsuarioComercialTable extends RolesTable { this: DBConfig =>
  import this.profile.api._
  /**
   *  Table mapped for usuarios_login
 *
   * @param tag Table tag
   */
  class UsuarioComercial(tag: Tag ) extends Table[UsuarioComercial]( tag, "usuarios_login" ) {
    implicit val timeMapper: BaseColumnType[ DateTime ] = JodatimeMapper.mapper()
    def username = column[ Option[ String ] ]( "username", O.PrimaryKey )
    def idRole = column[ Option[ Int ] ]( "idrole" )
    def last_login_ip = column[ Option[ String ] ]( "last_login_ip" )
    def last_login_date = column[ Option[ DateTime ] ]( "last_login_date" )
    def rolesFK = foreignKey( "fk_rol", idRole, roles )( _.id )
    override def * = ( username, idRole, last_login_ip, last_login_date ) <>((UsuarioComercial.apply _ ).tupled, UsuarioComercial.unapply )
  }
}

import java.sql.Timestamp
import CustomDriver.simple._

case class Usuario(id: Option[Int], correo: String, fechaActualizacion: Timestamp, identificacion: String,
                   tipoIdentificacion: Int, estado: Int, contrasena: Option[String], token: Option[String],
                   numeroIngresosErroneos: Int, ipUltimoIngreso: Option[String], fechaUltimoIngreso: Option[Timestamp])

class UsuarioTable(tag: Tag) extends Table[Usuario](tag, "USUARIO") {
  def id = column[Option[Int]]("ID", O.PrimaryKey, O.AutoInc)
  def correo = column[String]("CORREO")
  def fechaActualizacion = column[Timestamp]("FECHA_ACTUALIZACION")
  def identificacion = column[String]("IDENTIFICACION")
  def tipoIdentificacion = column[Int]("TIPO_IDENTIFICACION")
  def estado = column[Int]("ESTADO")
  def contrasena = column[Option[String]]("CONTRASENA")
  def token = column[Option[String]]("TOKEN")
  def numeroIngresosErroneos = column[Int]("NUMERO_INGRESOS_ERRONEOS")
  def ipUltimoIngreso = column[Option[String]]("IP_ULTIMO_INGRESO")
  def fechaUltimoIngreso = column[Option[Timestamp]]("FECHA_ULTIMO_INGRESO")

  def * = (id, correo, fechaActualizacion, identificacion, tipoIdentificacion, estado, contrasena, token,
    numeroIngresosErroneos, ipUltimoIngreso, fechaUltimoIngreso) <> (Usuario.tupled, Usuario.unapply)
}