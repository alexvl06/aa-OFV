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

  def getByIdentity(numeroIdentificacion: String): Future[Option[UsuarioComercial]] = {
    run(this.filter(_.identificacion === numeroIdentificacion).result.headOption)
  }

  def getById(idUsuario: Int): Future[Option[UsuarioComercial]] = {
    run(this.filter(_.id === idUsuario).result.headOption)
  }

  def getByToken(token: String): Future[Option[UsuarioComercial]] = {
    run(this.filter(_.token === token).result.headOption)
  }

  def getByEmail(correo: String): Future[Option[UsuarioComercial]] = {
    run(this.filter(_.correo === correo).result.headOption)
  }

  def getByPassword(pw_actual: String, idUsuario: Int): Future[Option[UsuarioComercial]] = {
    run(this.filter(x => x.contrasena === pw_actual && x.id === idUsuario).result.headOption)
  }

  def create(usuario: UsuarioComercial): Future[Int] = {
    run((this returning this.map(_.id)) += usuario)
  }

  def createToken(numeroIdentificacion: String, token: String): Future[Int] = {
    run(this.filter(_.identificacion === numeroIdentificacion).map(_.token).update(Some(token)))
  }

  def deleteToken(token: String): Future[Int] = {
    run(this.filter(_.token === token).map(_.token).update(Some(null)))
  }

  def updateIncorrectEntries(numeroIdentificacion: String, numeroIntentos: Int): Future[Int] = {
    run(this.filter(_.identificacion === numeroIdentificacion).map(_.numeroIngresosErroneos).update(numeroIntentos))
  }

  def updateIncorrectEntries(idUsuario: Int, numeroIntentos: Int): Future[Int] = {
    run(this.filter(_.id === idUsuario).map(_.numeroIngresosErroneos).update(numeroIntentos))
  }

  def updateLastIp(numeroIdentificacion: String, ipActual: String): Future[Int] = {
    run(this.filter(_.identificacion === numeroIdentificacion).map(_.ipUltimoIngreso).update(Some(ipActual)))
  }

  def updateLastDate(numeroIdentificacion: String, fechaActual: Timestamp): Future[Int] = {
    run(this.filter(_.identificacion === numeroIdentificacion).map(_.fechaUltimoIngreso).update(Some(fechaActual)))
  }

  def updateStateByIdentity(numeroIdentificacion: String, estado: Int): Future[Int] = {
    run(this.filter(_.identificacion === numeroIdentificacion).map(_.estado).update(estado))
  }

  def updateStateById(idUsuario: Int, estado: Int): Future[Int] = {
    run(this.filter(_.id === idUsuario).map(_.estado).update(estado))
  }

  def updatePassword(idUsuario: Int, password: String): Future[Int] = {
    run(this.filter(_.id === idUsuario).map(_.contrasena).update(password))
  }
}
