package portal.transaccional.fiduciaria.autenticacion.storage.daos.portal

import java.sql.Timestamp

import co.com.alianza.persistence.entities.Usuario

import scala.concurrent.Future

/**
 * Created by alexandra on 25/07/16.
 */
trait UsuarioDAOs {

  def getAll(): Future[Seq[Usuario]]

  def getByIdentity(numeroIdentificacion: String): Future[Option[Usuario]]

  def getByIdentityAndTypeId(numeroIdentificacion: String, tipoIdentificacion: Int): Future[Option[Usuario]]

  def getById(idUsuario: Int): Future[Option[Usuario]]

  def getByToken(token: String): Future[Option[Usuario]]

  def getByEmail(correo: String): Future[Option[Usuario]]

  def getByPassword(pw_actual: String, idUsuario: Int): Future[Option[Usuario]]

  def create(usuario: Usuario): Future[Int]

  def createToken(numeroIdentificacion: String, token: String): Future[Int]

  def deleteToken(token: String): Future[Int]

  def updateIncorrectEntries(numeroIdentificacion: String, numeroIntentos: Int): Future[Int]

  def updateIncorrectEntries(idUsuario: Int, numeroIntentos: Int): Future[Int]

  def updateLastIp(numeroIdentificacion: String, ipActual: String): Future[Int]

  def updateLastDate(idUsuario: Int, fechaActual: Timestamp): Future[Int]

  def updateUpdateDate(idUsuario: Int, fechaActual: Timestamp): Future[Int]

  def updateStateById(idUsuario: Int, estado: Int): Future[Int]

  def updatePassword(idUsuario: Int, password: String): Future[Int]

  /**
   * Consulta un usuario por el campo usuario
   * @param usuario Usuario del cliente
   * @return Option[Usuario]
   */
  def getUser(usuario: String): Future[Option[Usuario]]

  /**
   * Consulta un usuario por el campo usuario y correo
   * @param usuario
   * @param email
   * @return Option[Usuario]
   */
  def getUser(usuario: String, email: String): Future[Option[Usuario]]

  /**
   * Consulta un usuario por tipo de identificacion y numero de identificacion.
   * @param tipoId Tipo identificacion
   * @param identifiacion Numero de identificacion
   * @return Option[Usuario]
   */
  def getUser(tipoId: Int, identifiacion: String): Future[Option[Usuario]]

  /**
   * Consulta un cliente por Usuario, Tipo Identificacion y numero de identificacion.
   * @param usuario Usuario cliente
   * @param tipoId Tipo identificacion
   * @param identifiacion Numero de identificacion
   * @return Option[Usuario]
   */
  def getUser(usuario: String, tipoId: Int, identifiacion: String): Future[Option[Usuario]]

  /**
   * Consulta un cliente por Usuario, correo, Tipo Identificacion y numero de identificacion.
   * @param usuario Usuario cliente
   * @param email Correo del cliente.
   * @param tipoId Tipo identificacion
   * @param identifiacion Numero de identificacion
   * @return Option[Usuario]
   */
  def getUser(usuario: String, email: String, tipoId: Int, identifiacion: String): Future[Option[Usuario]]

  /**
   * Consulta si existe usuario numero de identificación
   * @param numeroIdentificacion Numero identificación de usuario a validar.
   * @return Boolean
   */
  def existsByIdentity(numeroIdentificacion: String): Future[Option[Usuario]]
}
