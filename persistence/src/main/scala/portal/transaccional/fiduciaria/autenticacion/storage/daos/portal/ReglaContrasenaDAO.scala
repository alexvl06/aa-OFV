package portal.transaccional.fiduciaria.autenticacion.storage.daos.portal

import portal.transaccional.fiduciaria.autenticacion.storage.config.DBConfig
import co.com.alianza.persistence.entities.{ ReglasContrasenas, ReglasContrasenasTable }
import slick.lifted.TableQuery

import scala.concurrent.Future

/**
 * Created by hernando on 25/07/16.
 */
case class ReglaContrasenaDAO()(implicit dcConfig: DBConfig) extends TableQuery(new ReglasContrasenasTable(_)) with ReglaContrasenaDAOs {

  import dcConfig.DB._
  import dcConfig.driver.api._

  def getAll(): Future[Seq[ReglasContrasenas]] = {
    run(this.result)
  }

  def getByKey(llave: String): Future[ReglasContrasenas] = {
    run(this.filter(_.llave === llave).result.head)
  }

}
