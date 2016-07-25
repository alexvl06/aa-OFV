package portal.transaccional.fiduciaria.autenticacion.storage.daos

import java.sql.Timestamp

import co.com.alianza.persistence.entities.Usuario

import scala.concurrent.Future

/**
 * Created by alexandra on 25/07/16.
 */
trait UsuarioDAOs {

  def getAll(): Future[Seq[Usuario]]

  def getByIdentity(numeroIdentificacion: String): Future[Option[Usuario]]

  def getById(idUsuario: Int): Future[Option[Usuario]]

  def getByToken(token: String): Future[Option[Usuario]]

  def getByEmail(correo: String): Future[Option[Usuario]]

  def getByPassword(pw_actual: String, idUsuario: Int): Future[Option[Usuario]]

  def create(usuario: Usuario): Future[Int]

  def createToken(numeroIdentificacion: String, token: String): Future[Int]

  def deleteToken(token: String): Future[Int]

  def updateIncorrectEntries(numeroIdentificacion: String, numeroIntentos: Int): Future[Int]

  def updateIncorrectEntries(idUsuario: Int, numeroIntentos: Int): Future[Int]

  def updateLastIp (numeroIdentificacion: String, ipActual: String): Future[Int]

  def updateLastDate(numeroIdentificacion: String, fechaActual: Timestamp): Future[Int]

  def updateStateByIdentity(numeroIdentificacion: String, estado: Int): Future[Int]

  def updateStateById(idUsuario: Int, estado: Int): Future[Int]

  def updatePassword(idUsuario: Int, password: String): Future[Int]
}
