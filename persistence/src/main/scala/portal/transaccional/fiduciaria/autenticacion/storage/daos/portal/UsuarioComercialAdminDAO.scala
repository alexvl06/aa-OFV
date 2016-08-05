package portal.transaccional.fiduciaria.autenticacion.storage.daos.portal

import java.sql.Timestamp

import co.com.alianza.persistence.config.DBConfig
import co.com.alianza.persistence.entities.{ UsuarioComercialAdmin, UsuarioComercialAdminTable }
import enumerations.EstadosEmpresaEnum
import slick.lifted.TableQuery

import scala.concurrent.Future

/**
 * Created by s4n on 2016
 */
case class UsuarioComercialAdminDAO()(implicit dcConfig: DBConfig) extends TableQuery(new UsuarioComercialAdminTable(_)) with UsuarioComercialAdminDAOs {

  import dcConfig.db._
  import dcConfig.profile.api._

  def getById(idUsuario: Int): Future[Option[UsuarioComercialAdmin]] = {
    run(this.filter(_.id === idUsuario).result.headOption)
  }

  def getByIdentityAndUser(identificacion: String, usuario: String): Future[Option[UsuarioComercialAdmin]] = {
    run(this.filter(u => u.identificacion === identificacion && u.usuario === usuario).result.headOption)
  }

  def getByNit(nitEmpresa: String): Future[Boolean] = {
    val estado = EstadosEmpresaEnum.activo.id
    val query = this.filter(u => u.identificacion === nitEmpresa && u.estado === estado).exists.result
    run(query)
  }

  def updateLastIp(idUsuario: Int, ipActual: String): Future[Int] = {
    run(this.filter(_.id === idUsuario).map(_.ipUltimoIngreso).update(Some(ipActual)))
  }

  def updateLastDate(idUsuario: Int, fechaActual: Timestamp): Future[Int] = {
    run(this.filter(_.id === idUsuario).map(_.fechaUltimoIngreso).update(Some(fechaActual)))
  }

  def updateStateById(idUsuario: Int, estado: Int): Future[Int] = {
    run(this.filter(_.id === idUsuario).map(_.estado).update(estado))
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

}
