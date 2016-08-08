package portal.transaccional.autenticacion.service.drivers.autorizacion

import co.com.alianza.exceptions.ValidacionAutorizacion
import co.com.alianza.infrastructure.dto.{ Usuario => UsuarioDTO }
import co.com.alianza.persistence.entities.Usuario

import scala.concurrent.Future

/**
 * Created by hernando on 27/07/16.
 */
trait AutorizacionUsuarioRepository {

  def autorizarUrl(token: String, url: String): Future[ValidacionAutorizacion]

  def invalidarToken(token: String): Future[Int]

}
