package portal.transaccional.fiduciaria.autenticacion.storage.daos.portal

import co.com.alianza.persistence.entities.{ RecursoAgenteInmobiliario, RecursoAgenteInmobiliarioTable }
import portal.transaccional.fiduciaria.autenticacion.storage.config.DBConfig
import slick.lifted.TableQuery

import scala.concurrent.Future

/**
 * Created by s4n in 2016
 */
case class RecursoInmobiliarioDAO()(implicit dcConfig: DBConfig) extends TableQuery(new RecursoAgenteInmobiliarioTable(_)) with RecursoInmobiliarioDAOs {

  import dcConfig.DB._
  import dcConfig.driver.api._

  def getAll(): Future[Seq[RecursoAgenteInmobiliario]] = {
    run(this.result)
  }

}
