package portal.transaccional.fiduciaria.autenticacion.storage.daos.portal

import co.com.alianza.persistence.entities.{ RecursoComercialTable, RolComercial, RolComercialTable, RolRecursoComercialTable }
import portal.transaccional.fiduciaria.autenticacion.storage.config.DBConfig
import portal.transaccional.fiduciaria.autenticacion.storage.helpers.AlianzaStorageHelper
import slick.lifted.TableQuery

import scala.concurrent.{ ExecutionContext, Future }

/**
 * Created by dfbaratov on 23/08/16.
 */
case class RolComercialDAO()(implicit val ec: ExecutionContext, dcConfig: DBConfig) extends TableQuery(new RolComercialTable(_)) with RolComercialDAOs with AlianzaStorageHelper {

  import dcConfig.DB._
  import dcConfig.driver.api._

  override def getAll(): Future[Seq[RolComercial]] = {
    run(this.result)
  }

}
