package portal.transaccional.fiduciaria.autenticacion.storage.daos.portal

import java.sql.Timestamp

import co.com.alianza.persistence.entities.{ UsuarioAgente, UsuarioAgenteTable }
import enumerations.EstadosEmpresaEnum
import portal.transaccional.fiduciaria.autenticacion.storage.config.DBConfig
import slick.lifted.TableQuery

import scala.concurrent.Future
import scala.reflect.{ ClassTag, _ }

/**
 * Created by s4n in 2016
 */
abstract class UsuarioAgenteDAO[T <: UsuarioAgenteTable[E], E <: UsuarioAgente : ClassTag](clazz: TableQuery[T])(implicit dcConfig: DBConfig) extends
  UsuarioAgenteDAOs[T,E] {

  val table: TableQuery[T] = clazz
  //lazy val clazzEntity = classTag[E].runtimeClass

  import dcConfig.DB._
  import dcConfig.driver.api._

  def update(id: Int, usuario: String, correo: String, nombreUsuario: String, cargo: String, descripcion: String): Future[Int] = {
    val query = table.filter(_.id === id).map(a => (a.correo, a.usuario, a.descripcion))
    run(query.update(correo, usuario, descripcion))
  }

  def updateStateByTime(idUsuarioAgenteEmpresarial: Int, estado: EstadosEmpresaEnum.estadoEmpresa, timestamp: Timestamp): Future[Int] = {
    val query = table.filter(u => u.id === idUsuarioAgenteEmpresarial).map(u => (u.estado, u.fechaActualizacion))
    run(query.update(estado.id, timestamp))
  }

  def updateIncorrectEntries(idUsuario: Int, numeroIntentos: Int): Future[Int] = {
    run(table.filter(_.id === idUsuario).map(_.numeroIngresosErroneos).update(numeroIntentos))
  }

  def updateLastIp(idUsuario: Int, ipActual: String): Future[Int] = {
    run(table.filter(_.id === idUsuario).map(_.ipUltimoIngreso).update(Some(ipActual)))
  }

  def updateLastEntryDate(idUsuario: Int, fechaActual: Timestamp): Future[Int] = {
    run(table.filter(_.id === idUsuario).map(_.fechaUltimoIngreso).update(Some(fechaActual)))
  }

  def updateStateById(idUsuario: Int, estado: Int): Future[Int] = {
    run(table.filter(_.id === idUsuario).map(_.estado).update(estado))
  }

  def updateState(idUsuario: Int, estado: Int): Future[Int] = {
    run(table.filter(_.id === idUsuario).map(_.estado).update(estado))
  }

  def getById(idUsuario: Int): Future[Option[E]] = {
    run(table.filter(_.id === idUsuario).result.headOption)
  }

  def getByIdentityAndUser(identificacion: String, usuario: String): Future[Option[E]] = {
    run(table.filter(u => u.identificacion === identificacion && u.usuario === usuario).result.headOption)
  }

  def isExists(idUsuario: Int, nit: String, usuario: String): Future[Boolean] = {
    run(table.filter(usu => usu.usuario === usuario && usu.identificacion === nit && usu.id =!= idUsuario).exists.result)
  }

  def updateToken(idUsuario: Int, token: String): Future[Int] = {
    run(table.filter(_.id === idUsuario).map(_.token).update(Some(token)))
  }

  def deleteToken(token: String): Future[Int] = {
    run(table.filter(_.token === token).map(_.token).update(Some(null)))
  }
}