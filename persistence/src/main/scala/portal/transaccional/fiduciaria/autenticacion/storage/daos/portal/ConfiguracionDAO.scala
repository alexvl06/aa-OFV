package portal.transaccional.fiduciaria.autenticacion.storage.daos.portal

import co.com.alianza.persistence.config.DBConfig
import co.com.alianza.persistence.entities.{ ConfiguracionesTable, Configuraciones }
import slick.lifted.TableQuery

import scala.concurrent.Future

/**
 * Created by hernando on 25/07/16.
 */
case class ConfiguracionDAO()(implicit dcConfig: DBConfig) extends TableQuery(new ConfiguracionesTable(_)) with ConfiguracionDAOs {

  import dcConfig.db._
  import dcConfig.profile.api._

  def getAll(): Future[Seq[Configuraciones]] = {
    run(this.result)
  }

  def getByKey(llave: String): Future[Configuraciones] = {
    run(this.filter(_.llave === llave).result.head)
  }

}
