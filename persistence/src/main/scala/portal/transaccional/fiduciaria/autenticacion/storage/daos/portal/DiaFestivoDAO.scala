package portal.transaccional.fiduciaria.autenticacion.storage.daos.portal

import java.sql.Date

import co.com.alianza.persistence.entities.{ DiaFestivo, DiaFestivoTable }
import portal.transaccional.fiduciaria.autenticacion.storage.config.DBConfig
import slick.lifted.TableQuery

import scala.concurrent.Future

/**
 * Created by s4n on 2016
 */
case class DiaFestivoDAO()(implicit dcConfig: DBConfig) extends TableQuery(new DiaFestivoTable(_)) with DiaFestivoDAOs {

  import dcConfig.DB._
  import dcConfig.driver.api._

  def obtener(fecha: Date): Future[Option[DiaFestivo]] = {
    run(this.filter(_.fecha === fecha).result.headOption)
  }

}
