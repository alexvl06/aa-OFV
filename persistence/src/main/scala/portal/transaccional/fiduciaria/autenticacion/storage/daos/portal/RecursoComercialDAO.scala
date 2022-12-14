package portal.transaccional.fiduciaria.autenticacion.storage.daos.portal

import co.com.alianza.persistence.entities.{ RecursoComercial, RecursoComercialTable, RolRecursoComercialTable }
import portal.transaccional.fiduciaria.autenticacion.storage.config.DBConfig
import portal.transaccional.fiduciaria.autenticacion.storage.helpers.AlianzaStorageHelper
import slick.lifted.TableQuery

import scala.concurrent.{ ExecutionContext, Future }

/**
 * Created by dfbaratov on 23/08/16.
 */
case class RecursoComercialDAO()(implicit val dcConfig: DBConfig) extends TableQuery(new RecursoComercialTable(_)) with RecursoComercialDAOs {

  import dcConfig.DB._
  import dcConfig.driver.api._

  override def getAll(): Future[Seq[RecursoComercial]] = {
    run(this.result)
  }

}
