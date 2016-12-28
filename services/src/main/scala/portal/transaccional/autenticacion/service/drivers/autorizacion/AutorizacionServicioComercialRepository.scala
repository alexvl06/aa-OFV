package portal.transaccional.autenticacion.service.drivers.autorizacion

import scala.concurrent.Future

/**
 * Created by dfbaratov on 23/08/16.
 */
trait AutorizacionServicioComercialRepository {

  def estaAutorizado(rolId: Int, url: String): Future[Boolean]

}
