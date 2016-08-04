package portal.transaccional.fiduciaria.autenticacion.storage.daos.portal

import co.com.alianza.persistence.config.DBConfig
import co.com.alianza.persistence.entities.{ IpsEmpresa, IpsEmpresaTable }
import slick.lifted.TableQuery

import scala.concurrent.Future

/**
 * Created by alexandra on 25/07/16.
 */
case class IpEmpresaDAO()(implicit dcConfig: DBConfig) extends TableQuery(new IpsEmpresaTable(_)) with IpEmpresaDAOs {

  import dcConfig.db._
  import dcConfig.profile.api._

  def getById(idEmpresa: Int): Future[Seq[IpsEmpresa]] = run(this.filter(_.idEmpresa === idEmpresa).result)

  def create(ip: IpsEmpresa): Future[String] = run(this returning this.map(_.ip) += ip)


}
