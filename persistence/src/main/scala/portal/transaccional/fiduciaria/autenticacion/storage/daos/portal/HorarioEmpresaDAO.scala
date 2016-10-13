package portal.transaccional.fiduciaria.autenticacion.storage.daos.portal

import co.com.alianza.persistence.entities.{ HorarioEmpresa, HorarioEmpresaTable }
import portal.transaccional.fiduciaria.autenticacion.storage.config.DBConfig
import slick.lifted.TableQuery

import scala.concurrent.Future

/**
 * Created by s4n on 2016
 */
case class HorarioEmpresaDAO()(implicit dcConfig: DBConfig) extends TableQuery(new HorarioEmpresaTable(_)) with HorarioEmpresaDAOs {

  import dcConfig.DB._
  import dcConfig.driver.api._

  def obtenerHorarioEmpresa(idEmpresa: Int): Future[Option[HorarioEmpresa]] = {
    run(this.filter(x => x.idEmpresa === idEmpresa).result.headOption)
  }
}
