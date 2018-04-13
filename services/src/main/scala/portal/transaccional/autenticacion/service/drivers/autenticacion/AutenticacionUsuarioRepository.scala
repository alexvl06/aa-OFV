package portal.transaccional.autenticacion.service.drivers.autenticacion

import portal.transaccional.autenticacion.service.web.autenticacion.UsuarioGenRequest

import scala.concurrent.Future
/**OFV LOGIN FASE 1**/
trait AutenticacionUsuarioRepository {
  /**
   * Autenticación general para usuario
   * @param usuarioGenRequest Peticion usuario
   * @param ip Ip donde llega la petición.
   * @return
   */
  def autenticarGeneral(usuarioGenRequest: UsuarioGenRequest, ip: String): Future[String]
}
