package portal.transaccional.autenticacion.service.drivers.usuarioAgenteInmobiliario

import co.com.alianza.exceptions.ValidacionAutorizacion

import scala.concurrent.Future

/**
 * Created by alexandra in 2016
 */
trait AutorizacionRepository {

  def autorizar(token: String, encriptedToken: String, url: Option[String], ip: String): Future[ValidacionAutorizacion]

}
