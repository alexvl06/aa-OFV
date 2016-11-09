package portal.transaccional.fiduciaria.autenticacion.storage.daos.portal

import java.sql.Timestamp

import co.com.alianza.persistence.entities.UsuarioAgenteEmpresarial
import enumerations.EstadosEmpresaEnum

import scala.concurrent.Future

/**
 * Created by hernando on 26/07/16.
 */
trait UsuarioEmpresarialDAOs {

  def create(agenteEmpresarial: UsuarioAgenteEmpresarial): Future[Int]

  def update(id: Int, usuario: String, correo: String, nombreUsuario: String, cargo: String, descripcion: String): Future[Int]

  def updateStateByTime(idUsuarioAgenteEmpresarial: Int, estado: EstadosEmpresaEnum.estadoEmpresa, timestamp: Timestamp): Future[Int]

  def updateIncorrectEntries(idUsuario: Int, numeroIntentos: Int): Future[Int]

  def updateLastIp(idUsuario: Int, ipActual: String): Future[Int]

  def updateUpdateDate(idUsuario: Int, fechaActual: Timestamp): Future[Int]

  def updateLastEntryDate(idUsuario: Int, fechaActual: Timestamp): Future[Int]

  def updateStateById(idUsuario: Int, estado: Int): Future[Int]

  def updatePasswordById(idUsuario: Int, contrasena: String): Future[Int]

  def updateState(idUsuario: Int, estado: Int): Future[Int]

  def getById(idUsuario: Int): Future[Option[UsuarioAgenteEmpresarial]]

  def getByIdentityAndUser(identificacion: String, usuario: String): Future[Option[UsuarioAgenteEmpresarial]]

  def isExists(idUsuario: Int, nit: String, usuario: String): Future[Boolean]

  def updateToken(idUsuario: Int, token: String): Future[Int]

  def deleteToken(token: String): Future[Int]

}
