package portal.transaccional.fiduciaria.autenticacion.storage.daos.portal

import java.sql.Timestamp

import co.com.alianza.persistence.config.DBConfig
import co.com.alianza.persistence.entities.{ UsuarioEmpresarialTable, UsuarioEmpresarial }
import slick.lifted.TableQuery

import scala.concurrent.Future

/**
 * Created by hernando on 26/07/16.
 */
case class UsuarioEmpresarialDAO(implicit dcConfig: DBConfig) extends TableQuery(new UsuarioEmpresarialTable(_)) with UsuarioEmpresarialDAOs {

  import dcConfig.db._
  import dcConfig.profile.api._

  def getByIdentityAndUser(identificacion: String, usuario: String): Future[Option[UsuarioEmpresarial]] = {
    run(this.filter(u => u.identificacion === identificacion && u.usuario === usuario).result.headOption)
  }

  def createToken(idUsuario: Int, token: String): Future[Int] = {
    println("createToken")
    println("createToken")
    println("createToken")
    println("createToken")
    run(this.filter(_.id === idUsuario).map(_.token).update(Some(token)))
  }

  def updateIncorrectEntries(idUsuario: Int, numeroIntentos: Int): Future[Int] = {
    run(this.filter(_.id === idUsuario).map(_.numeroIngresosErroneos).update(numeroIntentos))
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

  def deleteToken(token: String): Future[Int] = {
    run(this.filter(_.token === token).map(_.token).update(Some(null)))
  }

}
