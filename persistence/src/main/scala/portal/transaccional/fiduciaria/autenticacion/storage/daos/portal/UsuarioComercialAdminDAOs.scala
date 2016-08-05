package portal.transaccional.fiduciaria.autenticacion.storage.daos.portal

import java.sql.Timestamp

import co.com.alianza.persistence.entities.UsuarioComercialAdmin

import scala.concurrent.Future

/**
 * Created by s4n on 2016
 */
trait UsuarioComercialAdminDAOs {

  def getById(idUsuario: Int): Future[Option[UsuarioComercialAdmin]]

  def getByIdentityAndUser(identificacion: String, usuario: String): Future[Option[UsuarioComercialAdmin]]

  def getByNit(nitEmpresa: String): Future[Boolean]

  def updateIncorrectEntries(idUsuario: Int, numeroIntentos: Int): Future[Int]

  def updateLastIp(idUsuario: Int, ipActual: String): Future[Int]

  def updateLastDate(idUsuario: Int, fechaActual: Timestamp): Future[Int]

  def updateStateById(idUsuario: Int, estado: Int): Future[Int]

  def updatePassword(idUsuario: Int, password: String): Future[Int]

  def createToken(idUsuario: Int, token: String): Future[Int]

  def deleteToken(token: String): Future[Int]


}
