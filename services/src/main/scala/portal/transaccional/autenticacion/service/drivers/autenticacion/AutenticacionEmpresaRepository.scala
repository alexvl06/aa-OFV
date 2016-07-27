package portal.transaccional.autenticacion.service.drivers.autenticacion

import scala.concurrent.Future

/**
 * Created by hernando on 25/07/16.
 */
trait AutenticacionEmpresaRepository {

  def autenticarUsuarioEmpresa(numeroIdentificacion: String, usuario: String, password: String, clientIp: String): Future[String]

}
