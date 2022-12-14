package portal.transaccional.fiduciaria.autenticacion.storage.daos.portal

import co.com.alianza.persistence.entities.IpsEmpresa

import scala.concurrent.Future

/**
 * Created by hernando on 26/07/16.
 */
trait IpEmpresaDAOs {

  def getById(idEmpresa: Int): Future[Seq[IpsEmpresa]]

  def create(ip: IpsEmpresa): Future[String]

  def delete(ip: IpsEmpresa): Future[Int]

}
