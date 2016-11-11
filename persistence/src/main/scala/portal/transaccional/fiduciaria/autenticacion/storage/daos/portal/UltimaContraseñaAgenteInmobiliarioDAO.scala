package portal.transaccional.fiduciaria.autenticacion.storage.daos.portal

import co.com.alianza.persistence.entities.{ UltimaContrasenaAgenteInmobiliario, UltimaContrasenaUsuarioAgenteInmobiliarioTable }
import portal.transaccional.fiduciaria.autenticacion.storage.config.DBConfig
import slick.lifted.TableQuery

import scala.concurrent.Future

/**
 * Created by alexandra on 2016
 */
case class UltimaContraseñaAgenteInmobiliarioDAO()(implicit dcConfig: DBConfig) extends TableQuery(new UltimaContrasenaUsuarioAgenteInmobiliarioTable(_))
    with UltimaContraseñaAgenteInmobiliarioDAOs {

  import dcConfig.DB._
  import dcConfig.driver.api._

  def create(oldPass: UltimaContrasenaAgenteInmobiliario): Future[Int] = run((this returning this.map(_.id.get)) += oldPass)

  def findById(passwordValid: Int, idUser: Int): Future[Seq[UltimaContrasenaAgenteInmobiliario]] = {
    run(this.filter(_.idUsuario === idUser).sortBy(_.fechaUltimaContrasena.desc).take(passwordValid).result)
  }

}
