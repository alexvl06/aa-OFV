package portal.transaccional.autenticacion.service.drivers.usuarioAgenteEmpresarial

import co.com.alianza.persistence.entities.UsuarioAgente

import scala.concurrent.Future

trait UsuarioEmpresarialCustomRepository {

  def validacionBloqueoAdmin(usuario: UsuarioAgente): Future[Boolean]

  def validarUsuario(usuarioOption: Option[UsuarioAgente]): Future[UsuarioAgente]

  def actualizarContrasena(idUsuario: Int, contrasena: String): Future[Int]
}
