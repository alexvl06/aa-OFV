package portal.transaccional.fiduciaria.autenticacion.storage.daos.portal

import co.com.alianza.persistence.entities.{ Configuraciones, ConfiguracionesTable }
import portal.transaccional.fiduciaria.autenticacion.storage.config.DBConfig
import slick.lifted.TableQuery

import scala.concurrent.Future

/**
 * Created by seven4n 2016
 */
case class ConfiguracionDAO()(implicit dcConfig: DBConfig) extends TableQuery(new ConfiguracionesTable(_)) with ConfiguracionDAOs {

  import dcConfig.DB._
  import dcConfig.driver.api._

  def getAll(): Future[Seq[Configuraciones]] = {
    run(this.result)
  }

  def getByKey(llave: String): Future[Configuraciones] = {
    run(this.filter(_.llave === llave).result.head)
  }

}
