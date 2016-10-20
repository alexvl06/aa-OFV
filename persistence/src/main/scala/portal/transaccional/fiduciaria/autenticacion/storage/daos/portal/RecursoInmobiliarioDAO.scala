package portal.transaccional.fiduciaria.autenticacion.storage.daos.portal

import co.com.alianza.persistence.entities.{ RecursoGraficoInmobiliario, RecursoGraficoInmobiliarioTable }
import portal.transaccional.fiduciaria.autenticacion.storage.config.DBConfig
import slick.lifted.TableQuery

import scala.concurrent.Future

/**
 * Created by s4n in 2016
 */
case class RecursoInmobiliarioDAO()(implicit dcConfig: DBConfig) extends TableQuery(new RecursoGraficoInmobiliarioTable(_)) with RecursoInmobiliarioDAOs {

  import dcConfig.DB._
  import dcConfig.driver.api._

  def getAll(): Future[Seq[RecursoGraficoInmobiliario]] = run(this.filter(x => x.rol === 2 || x.rol === 3).result)

}
