package portal.transaccional.fiduciaria.autenticacion.storage.daos.portal

import portal.transaccional.fiduciaria.autenticacion.storage.config.DBConfig
import co.com.alianza.persistence.entities.{ PinUsuarioEmpresarialAdmin, PinUsuarioEmpresarialAdminTable }
import slick.lifted.TableQuery

import scala.concurrent.Future

/**
 * Created by S4N on 2016
 */
case class PinAdminDao()(implicit val dcConfig: DBConfig) extends TableQuery(new PinUsuarioEmpresarialAdminTable(_)) {

  import dcConfig.DB._
  import dcConfig.driver.api._

  def createPinAdmin(pinUsuario: PinUsuarioEmpresarialAdmin): Future[Int] = {
    run((this returning this.map(_.id.get)) += pinUsuario)
  }
}
