package portal.transaccional.autenticacion.service.drivers.autorizacion

import co.com.alianza.exceptions.ValidacionAutorizacion

import scala.concurrent.Future
import scala.util.control.NoStackTrace

/**
 * Created by s4n on 2016
 */
trait AutorizacionUsuarioEmpresarialAdminRepository {

  def autorizar(token: String, encriptedToken: String, url: String, ip: String, tipoCliente: String): Future[NoStackTrace]

  def invalidarToken(token: String, encriptedToken: String): Future[Int]

}
