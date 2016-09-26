package portal.transaccional.autenticacion.service.drivers.contrasenaAgenteInmobiliario

import scala.concurrent.Future

/**
 * Created by alexandra on 2016
 */
trait ContrasenaAgenteInmobiliarioRepository {

  def actualizarContrasena (token: String, pw_actual: String, pw_nuevo: String, idUsuario: Option[Int]): Future[Int]

}
