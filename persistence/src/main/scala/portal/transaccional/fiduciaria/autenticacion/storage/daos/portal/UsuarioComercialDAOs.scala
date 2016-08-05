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

  def getByPassword(pw_actual: String, idUsuario: Int): Future[Option[UsuarioComercial]]

  def create(usuario: UsuarioComercial): Future[Int]

  def createToken(numeroIdentificacion: String, token: String): Future[Int]

  def deleteToken(token: String): Future[Int]

  def updateIncorrectEntries(numeroIdentificacion: String, numeroIntentos: Int)

  def updateIncorrectEntries(idUsuario: Int, numeroIntentos: Int)

  def updateLastIp(numeroIdentificacion: String, ipActual: String)

  def updateLastDate(numeroIdentificacion: String, fechaActual: Timestamp)

  def updateStateByIdentity(numeroIdentificacion: String, estado: Int)

  def updateStateById(idUsuario: Int, estado: Int): Future[Int]

  def updatePassword(idUsuario: Int, password: String): Future[Int]
}
