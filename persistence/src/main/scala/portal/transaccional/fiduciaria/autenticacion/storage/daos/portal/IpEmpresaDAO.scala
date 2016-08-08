package portal.transaccional.fiduciaria.autenticacion.storage.daos.portal

import portal.transaccional.fiduciaria.autenticacion.storage.config.DBConfig
import co.com.alianza.persistence.entities.{ IpsEmpresa, IpsEmpresaTable }
import slick.lifted.TableQuery

import scala.concurrent.Future

/**
 * Created by alexandra on 25/07/16.
 */
case class IpEmpresaDAO()(implicit dcConfig: DBConfig) extends TableQuery(new IpsEmpresaTable(_)) with IpEmpresaDAOs {

  import dcConfig.DB._
  import dcConfig.driver.api._

  def getById(idEmpresa: Int): Future[Seq[IpsEmpresa]] = run(this.filter(_.idEmpresa === idEmpresa).result)

  def create(ip: IpsEmpresa): Future[String] = run(this returning this.map(_.ip) += ip)

}
