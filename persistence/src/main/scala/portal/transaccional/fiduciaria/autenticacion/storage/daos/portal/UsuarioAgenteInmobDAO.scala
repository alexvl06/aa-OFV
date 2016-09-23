package portal.transaccional.fiduciaria.autenticacion.storage.daos.portal

import co.com.alianza.persistence.entities.{UsuarioAgenteInmobiliario, UsuarioAgenteInmobiliarioTable}
import portal.transaccional.fiduciaria.autenticacion.storage.config.DBConfig
import slick.lifted.TableQuery

import scala.concurrent.Future

/**
  * Implementación del DAOde agentes inmobiliarios
  *
  * @param dcConfig Configuración de la base de datos
  */
case class UsuarioAgenteInmobDAO(implicit dcConfig: DBConfig) extends UsuarioAgenteDAO[UsuarioAgenteInmobiliarioTable, UsuarioAgenteInmobiliario](
  TableQuery[UsuarioAgenteInmobiliarioTable]) with UsuarioAgenteInmobDAOs {

  import dcConfig.DB._
  import dcConfig.driver.api._

  override def create(usuarioInmob: UsuarioAgenteInmobiliario): Future[Int] = {
    run((table returning table.map(_.id)) += usuarioInmob)
  }

  override def exists(id: Int, identificacion: String, usuario: String): Future[Boolean] = {
    isExists(id, identificacion, usuario)
  }

  override def get(identificacion: String, usuario: String): Future[Option[UsuarioAgenteInmobiliario]] = {
    getByIdentityAndUser(identificacion, usuario)
  }
}
