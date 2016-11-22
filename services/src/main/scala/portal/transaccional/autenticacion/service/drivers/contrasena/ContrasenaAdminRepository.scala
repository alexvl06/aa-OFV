package portal.transaccional.autenticacion.service.drivers.contrasena

import scala.concurrent.Future

/**
 * Created by hernando on 10/11/16.
 */
trait ContrasenaAdminRepository {

  def cambiarContrasena(idUsuario: Int, contrasena: String, contrasenaActual: String): Future[Int]

}
