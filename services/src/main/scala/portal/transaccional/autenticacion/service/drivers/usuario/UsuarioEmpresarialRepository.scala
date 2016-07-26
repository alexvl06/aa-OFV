package portal.transaccional.autenticacion.service.drivers.usuario

import java.sql.Timestamp

import co.com.alianza.persistence.entities.UsuarioEmpresarial

import scala.concurrent.Future

/**
 * Created by hernando on 26/07/16.
 */
trait UsuarioEmpresarialRepository {

  def getByIdentityAndUser(identificacion: String, usuario: String): Future[Option[UsuarioEmpresarial]]

  def validarUsuario(usuarioOption: Option[UsuarioEmpresarial], contrasena: String): Future[UsuarioEmpresarial]

  def validarExiste(usuarioOption: Option[UsuarioEmpresarial]): Future[UsuarioEmpresarial]

  def validarEstado(usuario: UsuarioEmpresarial): Future[Boolean]

  def validarContrasena(contrasena: String, usuario: UsuarioEmpresarial): Future[Boolean]

  def actualizarToken(idUsuario: Int, token: String): Future[Int]

  def actualizarIngresosErroneosUsuario(idUsuario: Int, numeroIntentos: Int): Future[Int]

  def actualizarIp(idUsuario: Int, ip: String): Future[Int]

  def actualizarFechaIngreso(idUsuario: Int, fechaActual: Timestamp): Future[Int]

}
