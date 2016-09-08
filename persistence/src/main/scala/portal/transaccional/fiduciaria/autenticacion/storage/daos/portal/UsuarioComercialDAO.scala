package portal.transaccional.fiduciaria.autenticacion.storage.daos.portal

import java.sql.Timestamp
import java.util.Date

import co.com.alianza.persistence.entities.{ UsuarioComercial, UsuarioComercialTable }
import portal.transaccional.fiduciaria.autenticacion.storage.config.DBConfig
import slick.lifted.TableQuery

import scala.concurrent.Future

/**
 * Created by s4n on 2016
 */
case class UsuarioComercialDAO()(implicit dcConfig: DBConfig) extends TableQuery(new UsuarioComercialTable(_)) with UsuarioComercialDAOs {

  import dcConfig.DB._
  import dcConfig.driver.api._

  def getAllUsers(): Future[Seq[UsuarioComercial]] = {
    run(this.result)
  }

  def getByUser(usuario: String): Future[Option[UsuarioComercial]] = {
    run(this.filter(_.usuario === usuario).result.headOption)
  }

  def getByToken(token: String): Future[Option[UsuarioComercial]] = {
    run(this.filter(_.token === token).result.headOption)
  }

  def create(nombreUsuario: String, ip: String): Future[Int] = {
    val fechaActual = Option(new Timestamp((new Date).getTime))
    run(this += UsuarioComercial(0, nombreUsuario, None, Some(ip), fechaActual))
  }

  def createToken(idUsuario: Int, token: String): Future[Int] = {
    run(this.filter(_.id === idUsuario).map(_.token).update(Some(token)))
  }

  def deleteToken(token: String): Future[Int] = {
    run(this.filter(_.token === token).map(_.token).update(Some(null)))
  }

  def updateIpFecha(nombreUsuario: String, ip: String): Future[Int] = {
    val fechaActual = Option(new Timestamp((new Date).getTime))
    val filtro = this.filter(_.usuario === nombreUsuario)
    run(filtro.map(n => (n.fechaUltimoIngreso, n.ipUltimoIngreso)).update(fechaActual, Some(ip)))
  }

  def existeUsuario(nombreUsuario: String): Future[Boolean] = {
    run(this.filter(_.usuario === nombreUsuario).exists.result)
  }

}
