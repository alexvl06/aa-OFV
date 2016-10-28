package portal.transaccional.fiduciaria.autenticacion.storage.daos.portal

import co.com.alianza.persistence.entities.{ PinUsuario, PinUsuarioTable }
import portal.transaccional.fiduciaria.autenticacion.storage.config.DBConfig
import slick.lifted.TableQuery

import scala.concurrent.Future

case class PinUsuarioDAO(implicit val dcConfig: DBConfig) extends TableQuery(new PinUsuarioTable(_)) with PinUsuarioDAOs {

  import dcConfig.DB._
  import dcConfig.driver.api._

  def create(pinUsuario: PinUsuario): Future[Int] = {
    run((this returning this.map(_.id.get)) += pinUsuario)
  }

  def findById(token: String): Future[Option[PinUsuario]] = {
    run(this.filter(_.tokenHash === token).result.headOption)
  }

  def delete(token: String): Future[Int] = {
    run(this.filter(_.tokenHash === token).delete)
  }

}
