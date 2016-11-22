package portal.transaccional.autenticacion.service.drivers.contrasena

import scala.concurrent.Future
/**
 * Created by hernando on 9/11/16.
 */
trait ContrasenaUsuarioRepository {
  def cambiarContrasena(idUsuario: Int, contrasena: String, contrasenaActual: String): Future[Int]
}
