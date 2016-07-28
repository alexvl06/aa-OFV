package portal.transaccional.fiduciaria.autenticacion.storage.daos.portal

import co.com.alianza.persistence.config.DBConfig
import co.com.alianza.persistence.entities.{ EmpresaTable, Empresa }
import slick.lifted.TableQuery

import scala.concurrent.Future

/**
 * Created by s4n on 2016
 */
case class EmpresaDAO()(implicit dcConfig: DBConfig) extends TableQuery(new EmpresaTable(_)) with EmpresaDAOs {

  import dcConfig.db._
  import dcConfig.profile.api._

  def getByNit(nit: String): Future[Option[Empresa]] = {
    run(this.filter(_.nit === nit).result.headOption)
  }

}
