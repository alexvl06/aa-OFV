package portal.transaccional.fiduciaria.autenticacion.storage.daos.portal

import portal.transaccional.fiduciaria.autenticacion.storage.config.DBConfig
import co.com.alianza.persistence.entities.{ PinUsuario, PinUsuarioTable }
import slick.lifted.TableQuery

import scala.concurrent.Future

class PinUsuarioDAO(implicit val dcConfig: DBConfig) extends TableQuery(new PinUsuarioTable(_)) with PinUsuarioDAOs {

  import dcConfig.DB._
  import dcConfig.driver.api._

  def create(pinUsuario: PinUsuario): Future[Int] = {
    run((this returning this.map(_.id.get)) += pinUsuario)
  }

}
