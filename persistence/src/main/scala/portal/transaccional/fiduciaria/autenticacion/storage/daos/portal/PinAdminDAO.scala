package portal.transaccional.fiduciaria.autenticacion.storage.daos.portal

import co.com.alianza.persistence.entities.{ PinAdmin, PinUsuarioEmpresarialAdminTable }
import portal.transaccional.fiduciaria.autenticacion.storage.config.DBConfig
import slick.lifted.TableQuery

import scala.concurrent.Future

/**
 * Created by S4N on 2016
 */
case class PinAdminDAO()(implicit val dcConfig: DBConfig) extends TableQuery(new PinUsuarioEmpresarialAdminTable(_)) with PinAdminDAOs {

  import dcConfig.DB._
  import dcConfig.driver.api._

  def create(pinUsuario: PinAdmin): Future[Int] = {
    run((this returning this.map(_.id.get)) += pinUsuario)
  }

  def findById(token: String): Future[Option[PinAdmin]] = {
    run(this.filter(_.token === token).result.headOption)
  }

  def delete(token: String): Future[Int] = {
    run(this.filter(_.token === token).delete)
  }

}
