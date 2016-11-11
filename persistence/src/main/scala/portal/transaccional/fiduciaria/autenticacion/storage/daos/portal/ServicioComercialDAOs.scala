package portal.transaccional.fiduciaria.autenticacion.storage.daos.portal

import co.com.alianza.persistence.entities.{ RecursoComercial, RolComercial }

import scala.concurrent.Future

/**
 * Created by s4n on 2016
 */
trait ServicioComercialDAOs {

  def autorizadoServicio(rolId: Int, url: String): Future[Option[RolComercial]]

  def existe(url: String): Future[Boolean]

}
