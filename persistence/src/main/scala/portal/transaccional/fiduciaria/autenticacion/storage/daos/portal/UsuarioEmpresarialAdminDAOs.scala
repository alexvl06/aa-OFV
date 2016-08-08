package portal.transaccional.fiduciaria.autenticacion.storage.daos.portal

import java.sql.Timestamp

import co.com.alianza.persistence.entities.UsuarioEmpresarialAdmin

import scala.concurrent.Future

/**
 * Created by hernando on 26/07/16.
 */
trait UsuarioEmpresarialAdminDAOs {

  def getById(idUsuario: Int): Future[Option[UsuarioEmpresarialAdmin]]

  def getByIdentityAndUser(identificacion: String, usuario: String): Future[Option[UsuarioEmpresarialAdmin]]

  def createToken(idUsuario: Int, token: String): Future[Int]

  def deleteToken(token: String): Future[Int]

  def updateIncorrectEntries(idUsuario: Int, numeroIntentos: Int): Future[Int]

  def updateLastIp(idUsuario: Int, ip: String): Future[Int]

  def updateLastDate(idUsuario: Int, fechaActual: Timestamp): Future[Int]

  def updateStateById(idUsuario: Int, estado: Int): Future[Int]

}
