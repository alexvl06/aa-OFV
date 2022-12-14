package portal.transaccional.fiduciaria.autenticacion.storage.daos.portal

import portal.transaccional.fiduciaria.autenticacion.storage.config.DBConfig
import co.com.alianza.persistence.entities.IpsUsuario
import co.com.alianza.persistence.entities.IpsUsuarioTable
import slick.driver.{ JdbcProfile, PostgresDriver }
import slick.lifted.TableQuery

import scala.concurrent.Future

/**
 * Created by alexandra on 25/07/16.
 */
case class IpUsuarioDAO(implicit val dcConfig: DBConfig) extends TableQuery(new IpsUsuarioTable(_)) with IpUsuarioDAOs {

  import dcConfig.DB._
  import dcConfig.driver.api._

  def getById(idUsuario: Int): Future[Seq[IpsUsuario]] = {
    run(this.filter(_.idUsuario === idUsuario).result)
  }

  def getByUsuarioIp(idUsuario: Int, ip: String): Future[Option[IpsUsuario]] = {
    run(this.filter(x => x.idUsuario === idUsuario && x.ip === ip).result.headOption)
  }

  def create(ip: IpsUsuario): Future[String] = {
    run((this returning this.map(_.ip)) += ip)
  }

  def delete(ipsUsuarioE: IpsUsuario): Future[Int] = {
    run(this.filter(x => x.idUsuario === ipsUsuarioE.idUsuario && x.ip === ipsUsuarioE.ip).delete)
  }

}