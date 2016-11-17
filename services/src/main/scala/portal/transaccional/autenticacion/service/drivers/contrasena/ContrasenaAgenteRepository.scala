package portal.transaccional.autenticacion.service.drivers.contrasena

import co.com.alianza.infrastructure.dto.security.UsuarioAuth

import scala.concurrent.Future

/**
 * Created by hernando on 10/11/16.
 */
trait ContrasenaAgenteRepository {

  def reiniciarContrasena(admin: UsuarioAuth, usuarioAgente: String): Future[Boolean]

  def cambiarEstado(admin: UsuarioAuth, usuarioAgente: String): Future[Boolean]

}
