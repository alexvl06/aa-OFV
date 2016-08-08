package portal.transaccional.fiduciaria.autenticacion.storage.daos.portal

import portal.transaccional.fiduciaria.autenticacion.storage.config.DBConfig
import co.com.alianza.persistence.entities.EmpresaUsuarioAdminTable
import portal.transaccional.fiduciaria.autenticacion.storage.config.DBConfig
import slick.lifted.TableQuery

import scala.concurrent.Future

/**
 * Created by s4n on 2016
 */
case class EmpresaAdminDAO()(implicit dcConfig: DBConfig) extends TableQuery(new EmpresaUsuarioAdminTable(_)) with EmpresaAdminDAOs {

  import dcConfig.DB._
  import dcConfig.driver.api._

  def obtenerIdEmpresa(idUsuario: Int): Future[Int] = {
    run(this.filter(_.idUsuario === idUsuario).map(_.idEmpresa).result.head)
  }
}
