package portal.transaccional.fiduciaria.autenticacion.storage.daos.portal

import java.sql.Timestamp

import co.com.alianza.persistence.entities.UsuarioComercial

import scala.concurrent.Future

/**
 * Created by s4n on 2016
 */
trait UsuarioComercialDAOs {

  def getAllUsers(): Future[Seq[UsuarioComercial]]

  def getByUser(usuario: String): Future[Option[UsuarioComercial]]

  def getByToken(token: String): Future[Option[UsuarioComercial]]

  def create(usuario: UsuarioComercial): Future[Int]

  def createToken(idUsuario: Int, token: String): Future[Int]

  def deleteToken(token: String): Future[Int]

  def updateLastIp(idUsuario: Int, ipActual: String): Future[Int]

  def updateLastDate(idUsuario: Int, fechaActual: Timestamp): Future[Int]

  def update(usuario: String, ip: String) : Future[Int]

}
