package portal.transaccional.autenticacion.service.drivers.usuarioAgenteInmobiliario

import co.com.alianza.persistence.entities.UsuarioAgenteInmobiliario

import scala.concurrent.Future

/**
 * Created by s4n in 2016
 */
trait UsuarioAgenteInmobiliarioRepository {

  def getByIdentityAndUser(identificacion: String, usuario: String): Future[Option[UsuarioAgenteInmobiliario]]

  def actualizarIngresosErroneosUsuario(idUsuario: Int, numeroIntentos: Int): Future[Int]

}
