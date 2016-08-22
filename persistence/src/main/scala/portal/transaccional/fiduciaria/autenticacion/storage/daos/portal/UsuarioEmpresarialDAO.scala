package portal.transaccional.fiduciaria.autenticacion.storage.daos.portal

import java.sql.Timestamp

import portal.transaccional.fiduciaria.autenticacion.storage.config.DBConfig
import co.com.alianza.persistence.entities.{ UsuarioEmpresarial, UsuarioEmpresarialTable }
import enumerations.EstadosEmpresaEnum
import slick.lifted.TableQuery

import scala.concurrent.Future

/**
 * Created by s4n on 2016
 */
case class UsuarioEmpresarialDAO(implicit dcConfig: DBConfig) extends TableQuery(new UsuarioEmpresarialTable(_)) with UsuarioEmpresarialDAOs {

  import dcConfig.DB._
  import dcConfig.driver.api._

  def create(agenteEmpresarial: UsuarioEmpresarial): Future[Int] = {
    run((this returning this.map(_.id)) += agenteEmpresarial)
  }

  def update(id: Int, usuario: String, correo: String, nombreUsuario: String, cargo: String, descripcion: String): Future[Int] = {
    val query = this.filter(_.id === id).map(a => (a.correo, a.usuario, a.nombreUsuario, a.cargo, a.descripcion))
    run(query.update(correo, usuario, nombreUsuario, cargo, descripcion))
  }

  def updateStateByTime(idUsuarioAgenteEmpresarial: Int, estado: EstadosEmpresaEnum.estadoEmpresa, timestamp: Timestamp): Future[Int] = {
    val query = this.filter(u => u.id === idUsuarioAgenteEmpresarial).map(u => (u.estado, u.fechaActualizacion))
    run(query.update(estado.id, timestamp))
  }

  def updateIncorrectEntries(idUsuario: Int, numeroIntentos: Int): Future[Int] = {
    run(this.filter(_.id === idUsuario).map(_.numeroIngresosErroneos).update(numeroIntentos))
  }

  def updateLastIp(idUsuario: Int, ipActual: String): Future[Int] = {
    run(this.filter(_.id === idUsuario).map(_.ipUltimoIngreso).update(Some(ipActual)))
  }

  def updateLastEntryDate(idUsuario: Int, fechaActual: Timestamp): Future[Int] = {
    run(this.filter(_.id === idUsuario).map(_.fechaUltimoIngreso).update(Some(fechaActual)))
  }

  def updateStateById(idUsuario: Int, estado: Int): Future[Int] = {
    run(this.filter(_.id === idUsuario).map(_.estado).update(estado))
  }

  def updateState(idUsuario: Int, estado: Int): Future[Int] = {
    run(this.filter(_.id === idUsuario).map(_.estado).update(estado))
  }

  def getById(idUsuario: Int): Future[Option[UsuarioEmpresarial]] = {
    run(this.filter(_.id === idUsuario).result.headOption)
  }

  def getByIdentityAndUser(identificacion: String, usuario: String): Future[Option[UsuarioEmpresarial]] = {
    run(this.filter(u => u.identificacion === identificacion && u.usuario === usuario).result.headOption)
  }

  def isExists(idUsuario: Int, nit: String, usuario: String): Future[Boolean] = {
    run(this.filter(usu => usu.usuario === usuario && usu.identificacion === nit && usu.id =!= idUsuario).exists.result)
  }

  def updateToken(idUsuario: Int, token: String): Future[Int] = {
    run(this.filter(_.id === idUsuario).map(_.token).update(Some(token)))
  }

  def deleteToken(token: String): Future[Int] = {
    run(this.filter(_.token === token).map(_.token).update(Some(null)))
  }

}
