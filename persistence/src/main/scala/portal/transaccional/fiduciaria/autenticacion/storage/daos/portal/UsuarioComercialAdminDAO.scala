package portal.transaccional.fiduciaria.autenticacion.storage.daos.portal

import java.sql.Timestamp

import co.com.alianza.persistence.entities.{ UsuarioComercialAdmin, UsuarioComercialAdminTable }
import portal.transaccional.fiduciaria.autenticacion.storage.config.DBConfig
import slick.lifted.TableQuery

import scala.concurrent.Future

/**
 * Created by s4n on 2016
 */
case class UsuarioComercialAdminDAO()(implicit dcConfig: DBConfig) extends TableQuery(new UsuarioComercialAdminTable(_)) with UsuarioComercialAdminDAOs {

  import dcConfig.DB._
  import dcConfig.driver.api._

  def getById(idUsuario: Int): Future[Option[UsuarioComercialAdmin]] = {
    run(this.filter(_.id === idUsuario).result.headOption)
  }

  def getByUser(usuario: String): Future[Option[UsuarioComercialAdmin]] = {
    run(this.filter(_.usuario === usuario).result.headOption)
  }

  def getByToken(token: String): Future[Option[UsuarioComercialAdmin]] = {
    run(this.filter(_.token === token).result.headOption)
  }

  def getByEmail(correo: String): Future[Option[UsuarioComercialAdmin]] = {
    run(this.filter(_.correo === correo).result.headOption)
  }

  def updateLastIp(idUsuario: Int, ipActual: String): Future[Int] = {
    run(this.filter(_.id === idUsuario).map(_.ipUltimoIngreso).update(Some(ipActual)))
  }

  def updateLastDate(idUsuario: Int, fechaActual: Timestamp): Future[Int] = {
    run(this.filter(_.id === idUsuario).map(_.fechaUltimoIngreso).update(Some(fechaActual)))
  }

  def updatePassword(idUsuario: Int, password: String): Future[Int] = {
    val query = this.filter(_.id === idUsuario).map(admin => admin.contrasena)
    run(query.update(Some(password)))
  }

  def createToken(idUsuario: Int, token: String): Future[Int] = {
    run(this.filter(_.id === idUsuario).map(_.token).update(Some(token)))
  }

  def deleteToken(token: String): Future[Int] = {
    run(this.filter(_.token === token).map(_.token).update(Some(null)))
  }

  def create(usuarioComercialAdmin: UsuarioComercialAdmin): Future[Int] = {
    run((this returning this.map(_.id)) += usuarioComercialAdmin)
  }
}
