package portal.transaccional.fiduciaria.autenticacion.storage.daos.portal

import co.com.alianza.persistence.entities.PermisoAgenteInmobiliario

import scala.concurrent.Future

trait PermisoInmobiliarioDAOs {

  def update(eliminados: Seq[PermisoAgenteInmobiliario], agregados: Seq[PermisoAgenteInmobiliario]): Future[Option[Int]]
}
