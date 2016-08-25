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

  def create(nombreUsuario: String, ip: String): Future[Int]

  def createToken(idUsuario: Int, token: String): Future[Int]

  def deleteToken(token: String): Future[Int]

  def updateIpFecha(nombreUsuario: String, ip: String): Future[Int]

  def existeUsuario(nombreUsuario: String): Future[Boolean]

}
