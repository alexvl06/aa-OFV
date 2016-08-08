package portal.transaccional.autenticacion.service.drivers.autorizacion

import scala.concurrent.Future

/**
 * Created by s4n on 2016
 */
trait AutorizacionUsuarioComercialRepository {

  def invalidarToken(token: String, encriptedToken: String): Future[Int]

}
