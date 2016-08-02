package portal.transaccional.autenticacion.service.drivers.autorizacion

import co.com.alianza.exceptions.ValidacionAutorizacion

import scala.concurrent.Future

/**
 * Created by alexandra on 2016
 */
trait AutorizacionUsuarioEmpresarialRepository {

  def autorizar(token: String, url: String, ip: String): Future[ValidacionAutorizacion]

}
