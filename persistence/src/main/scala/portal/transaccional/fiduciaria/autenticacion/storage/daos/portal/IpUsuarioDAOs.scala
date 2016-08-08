package portal.transaccional.fiduciaria.autenticacion.storage.daos.portal

import co.com.alianza.persistence.entities.IpsUsuario

import scala.concurrent.Future

/**
 * Created by s4n on 2016
 */
trait IpUsuarioDAOs {

  def getById(idUsuario: Int): Future[Seq[IpsUsuario]]

  def getAll(): Future[Seq[IpsUsuario]]

  def getByUsuarioIp(idUsuario: Int, ip: String): Future[Option[IpsUsuario]]

  def create(ip: IpsUsuario): Future[String]

  //def delete(ipsUsuarioE: IpsUsuario): Future[Int]

}