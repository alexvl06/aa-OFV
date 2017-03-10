package portal.transaccional.fiduciaria.autenticacion.storage.daos.portal

import co.com.alianza.persistence.entities.{ UltimaContrasena, UltimaContrasenaAdminTable }
import portal.transaccional.fiduciaria.autenticacion.storage.config.DBConfig
import slick.lifted.TableQuery

import scala.concurrent.Future

/**
 * Created by hernando on 26/10/16.
 */
case class UltimaContrasenaAdminDAO()(implicit val dcConfig: DBConfig) extends TableQuery(new UltimaContrasenaAdminTable(_)) with UltimaContrasenaDAOs {

  import dcConfig.DB._
  import dcConfig.driver.api._

  def create(ultimaContrasena: UltimaContrasena): Future[Int] = {
    run((this returning this.map(_.id.get)) += ultimaContrasena)
  }

  def getByAmount(idUsuario: Int, cantidad: Int): Future[Seq[UltimaContrasena]] = {
    run(this.filter(_.idUsuario === idUsuario).sortBy(_.fechaUltimaContrasena.desc).take(cantidad).result)
  }

}
