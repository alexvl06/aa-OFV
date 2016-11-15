package portal.transaccional.fiduciaria.autenticacion.storage.daos.portal

import co.com.alianza.persistence.entities.{ PinAgente, PinEmpresaTable }
import portal.transaccional.fiduciaria.autenticacion.storage.config.DBConfig
import slick.lifted.TableQuery

import scala.concurrent.Future

/**
 * Created by hernando on 26/10/16.
 */
case class PinAgenteDAO()(implicit val dcConfig: DBConfig) extends TableQuery(new PinEmpresaTable(_)) with PinAgenteDAOs {

  import dcConfig.DB._
  import dcConfig.driver.api._

  def create(pinAgente: PinAgente): Future[Int] = {
    run((this returning this.map(_.id.get)) += pinAgente)
  }

  def findById(token: String): Future[Option[PinAgente]] = {
    run(this.filter(_.tokenHash === token).result.headOption)
  }

  def delete(token: String): Future[Int] = {
    run(this.filter(_.tokenHash === token).delete)
  }

  def deleteAll(idUsuario: Int): Future[Int] = {
    run(this.filter(_.idUsuarioEmpresarial === idUsuario).delete)
  }

}
