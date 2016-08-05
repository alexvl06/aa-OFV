package portal.transaccional.fiduciaria.autenticacion.storage.daos.portal

import java.sql.Timestamp

import co.com.alianza.persistence.entities.UsuarioComercial

import scala.concurrent.Future

/**
 * Created by s4n on 2016
 */
trait UsuarioComercialDAOs {

  def getAllUsers(): Future[Seq[UsuarioComercial]]

  def getByIdentity(numeroIdentificacion: String): Future[Option[UsuarioComercial]]

  def getById(idUsuario: Int): Future[Option[UsuarioComercial]]

  def getByToken(token: String): Future[Option[UsuarioComercial]]

  def getByEmail(correo: String): Future[Option[UsuarioComercial]]

  def create(usuario: UsuarioComercial): Future[Int]

  def createToken(numeroIdentificacion: String, token: String): Future[Int]

  def deleteToken(token: String): Future[Int]

  def updateLastIp(numeroIdentificacion: String, ipActual: String): Future[Int]

  def updateLastDate(numeroIdentificacion: String, fechaActual: Timestamp): Future[Int]

  def updateStateByIdentity(numeroIdentificacion: String, estado: Int): Future[Int]

  def updateStateById(idUsuario: Int, estado: Int): Future[Int]
}
