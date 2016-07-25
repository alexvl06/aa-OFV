package portal.transaccional.fiduciaria.autenticacion.storage.daos.portal

import co.com.alianza.persistence.config.DBConfig
import co.com.alianza.persistence.entities.{ PinUsuario, PinUsuarioTable }
import slick.lifted.TableQuery

import scala.concurrent.Future

class PinUsuarioDAO(implicit val dcConfig: DBConfig) extends TableQuery(new PinUsuarioTable(_)) {

  import dcConfig.db._
  import dcConfig.profile.api._

  def create(pinUsuario: PinUsuario): Future[Int] = {
    run((this returning this.map(_.id.get)) += pinUsuario)
  }

}
