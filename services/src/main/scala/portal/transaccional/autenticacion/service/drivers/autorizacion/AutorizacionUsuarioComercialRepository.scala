package portal.transaccional.autenticacion.service.drivers.autorizacion

import co.com.alianza.exceptions.ValidacionAutorizacion

import scala.concurrent.Future

/**
 * Created by s4n on 2016
 */
trait AutorizacionUsuarioComercialRepository {

  def invalidarToken(token: String, encriptedToken: String): Future[Int]

  def invalidarTokenSAC(token: String, encriptedToken: String): Future[Int]

  def autorizar(token: String, encriptedToken: String, url: String): Future[ValidacionAutorizacion]

  def autorizarSAC(token: String, encriptedToken: String, url: String): Future[ValidacionAutorizacion]

}
