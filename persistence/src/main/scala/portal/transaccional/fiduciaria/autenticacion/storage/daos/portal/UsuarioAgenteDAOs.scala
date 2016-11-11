package portal.transaccional.fiduciaria.autenticacion.storage.daos.portal

import java.sql.Timestamp

import co.com.alianza.persistence.entities.{ UsuarioAgente, UsuarioAgenteTable }
import enumerations.EstadosEmpresaEnum

import scala.concurrent.Future

/**
 * Created by s4n on 2016
 */
trait UsuarioAgenteDAOs[T <: UsuarioAgenteTable[E], E <: UsuarioAgente] {

  //def update(id: Int, usuario: String, correo: String, nombreUsuario: String, cargo: String, descripcion: String): Future[Int]

  def updateStateByTime(idUsuarioAgenteEmpresarial: Int, estado: EstadosEmpresaEnum.estadoEmpresa, timestamp: Timestamp): Future[Int]

  def updateIncorrectEntries(idUsuario: Int, numeroIntentos: Int): Future[Int]

  def updateLastIp(idUsuario: Int, ipActual: String): Future[Int]

  def updateLastEntryDate(idUsuario: Int, fechaActual: Timestamp): Future[Int]

  def updateStateById(idUsuario: Int, estado: Int): Future[Int]

  def updateState(idUsuario: Int, estado: Int): Future[Int]

  def getById(idUsuario: Int): Future[Option[E]]

  def getByIdentityAndUser(identificacion: String, usuario: String): Future[Option[E]]

  def isExists(idUsuario: Int, nit: String, usuario: String): Future[Boolean]

  def updateToken(idUsuario: Int, token: String): Future[Int]

  def deleteToken(token: String): Future[Int]
}
