package portal.transaccional.fiduciaria.autenticacion.storage.daos.portal

import java.sql.Timestamp

import co.com.alianza.persistence.config.DBConfig
import co.com.alianza.persistence.entities.{ UsuarioComercial, UsuarioComercialTable }
import slick.lifted.TableQuery

import scala.concurrent.Future

/**
 * Created by s4n on 2016
 */
case class UsuarioComercialDAO()(implicit dcConfig: DBConfig) extends TableQuery(new UsuarioComercialTable(_)) with UsuarioComercialDAOs {

  import dcConfig.db._
  import dcConfig.profile.api._

  def getAllUsers(): Future[Seq[UsuarioComercial]] = {
    run(this.result)
  }

  def getByUser(usuario: String): Future[Option[UsuarioComercial]] = {
    run(this.filter(_.usuario === usuario).result.headOption)
  }

  def getByToken(token: String): Future[Option[UsuarioComercial]] = {
    run(this.filter(_.token === token).result.headOption)
  }

  def create(usuario: UsuarioComercial): Future[Int] = {
    run((this returning this.map(_.id)) += usuario)
  }

  def createToken(idUsuario: Int, token: String): Future[Int] = {
    run(this.filter(_.id === idUsuario).map(_.token).update(Some(token)))
  }

  def deleteToken(token: String): Future[Int] = {
    run(this.filter(_.token === token).map(_.token).update(Some(null)))
  }

  def updateLastIp(usuario: String, ipActual: String): Future[Int] = {
    run(this.filter(_.usuario === usuario).map(_.ipUltimoIngreso).update(Some(ipActual)))
  }

  def updateLastDate(usuario: String, fechaActual: Timestamp): Future[Int] = {
    run(this.filter(_.usuario === usuario).map(_.fechaUltimoIngreso).update(Some(fechaActual)))
  }

}
