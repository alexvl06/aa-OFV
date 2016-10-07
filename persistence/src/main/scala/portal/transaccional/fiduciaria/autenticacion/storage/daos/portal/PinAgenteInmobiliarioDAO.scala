package portal.transaccional.fiduciaria.autenticacion.storage.daos.portal

import co.com.alianza.persistence.entities.{PinAgenteInmobiliario, PinAgenteInmobiliarioTable}
import portal.transaccional.fiduciaria.autenticacion.storage.config.DBConfig
import slick.lifted.TableQuery

import scala.concurrent.Future

/**
  * Implementación de las operaciones de persistencia sobre la entidad PinAgenteInmobiliario
  * definidas en la interafaz PinAgenteInmobiliarioDAOs
  *
  * @param dcConfig Configuración de la base de datos
  */
case class PinAgenteInmobiliarioDAO(implicit val dcConfig: DBConfig)
  extends TableQuery(new PinAgenteInmobiliarioTable(_)) with PinAgenteInmobiliarioDAOs {

  import dcConfig.DB._
  import dcConfig.driver.api._

  def create(pin: PinAgenteInmobiliario): Future[Option[Int]] = {
    run((this returning this.map(_.id)) += pin)
  }

  def get(tokenHash: String): Future[Option[PinAgenteInmobiliario]] = {
    run(this.filter(_.tokenHash === tokenHash).result.headOption)
  }
}
