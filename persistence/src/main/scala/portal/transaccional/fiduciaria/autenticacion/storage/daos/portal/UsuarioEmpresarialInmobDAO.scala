package portal.transaccional.fiduciaria.autenticacion.storage.daos.portal

import co.com.alianza.persistence.entities.{ UsuarioAgenteInmobiliario, UsuarioAgenteInmobiliarioTable, UsuarioEmpresarial }
import portal.transaccional.fiduciaria.autenticacion.storage.config.DBConfig
import slick.lifted.TableQuery

import scala.concurrent.Future

/**
 * Created by alexandra on 2016
 */
case class UsuarioEmpresarialInmobDAO() (implicit dcConfig: DBConfig) extends TableQuery(new UsuarioAgenteInmobiliarioTable(_)) with UsuarioEmpresarialAdminDAOs {

  import dcConfig.DB._
  import dcConfig.driver.api._

  def create(agenteInmob: UsuarioAgenteInmobiliario): Future[Int] = {
    run((this returning this.map(_.id)) += agenteInmob)
  }

  def updateStateById(idUsuario: Int, estado: Int): Future[Int] = {
    run(this.filter(_.id === idUsuario).map(_.estado).update(estado))
  }

  def getByIdentityAndUser(idEmpresa: String, usuario: String): Future[Option[UsuarioAgenteInmobiliario]] = {
    run(this.filter(u => u.idEmpresa === idEmpresa && u.usuario === usuario).result.headOption)
  }


}
