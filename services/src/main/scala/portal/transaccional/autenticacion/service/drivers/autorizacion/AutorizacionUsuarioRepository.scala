package portal.transaccional.autenticacion.service.drivers.autorizacion

import co.com.alianza.infrastructure.dto.RecursoUsuario
import co.com.alianza.persistence.entities.Usuario

import scala.concurrent.Future

/**
 * Created by hernando on 27/07/16.
 */
trait AutorizacionUsuarioRepository {

  def autorizarUrl(token: String, url: String): Future[Boolean]

  def validarUsario(usuarioOption: Option[Usuario]): Future[Usuario]

  def validarToken(token: String): Future[Boolean]

}
