package portal.transaccional.autenticacion.service.drivers.usuario

import co.com.alianza.persistence.entities.Usuario

import scala.concurrent.Future

/**
 * Created by hernando on 25/07/16.
 */
trait UsuarioRepository {

  def getByIdentificacion(numeroIdentificacion: String): Future[Usuario]

  def validarEstados(estadoUsuario: Int): Future[Boolean]

  def validarContrasena(contrasena: String, contrasenaUsuario: String, idUsuario: Int): Future[Boolean]

  def actualizarToken(numeroIdentificacion: String, token: String): Future[Int]

}
