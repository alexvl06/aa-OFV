package portal.transaccional.autenticacion.service.drivers.usuarioAdmin

import java.sql.Timestamp

import co.com.alianza.commons.enumerations.TiposCliente.TiposCliente
import co.com.alianza.persistence.entities.UsuarioEmpresarialAdmin

import scala.concurrent.Future

/**
 * Created by hernando on 26/07/16.
 */
trait UsuarioEmpresarialAdminRepository {

  def getByIdentityAndUser(identificacion: String, usuario: String): Future[Option[UsuarioEmpresarialAdmin]]

  def actualizarToken(idUsuario: Int, token: String): Future[Int]

  def actualizarIngresosErroneosUsuario(idUsuario: Int, numeroIntentos: Int): Future[Int]

  def actualizarIp(idUsuario: Int, ip: String): Future[Int]

  def actualizarFechaIngreso(idUsuario: Int, fechaActual: Timestamp): Future[Int]

  def actualizarInfoUsuario(usuario: UsuarioEmpresarialAdmin, ip: String): Future[Int]

  def validarUsuario(usuario: UsuarioEmpresarialAdmin, contrasena: String, reintentosErroneos: Int): Future[Boolean]

  def validarEstado(usuario: UsuarioEmpresarialAdmin): Future[Boolean]

  def validarContrasena(contrasena: String, usuario: UsuarioEmpresarialAdmin, reintentosErroneos: Int): Future[Boolean]

  def validarCaducidadContrasena(tipoCliente: TiposCliente, usuario: UsuarioEmpresarialAdmin, dias: Int): Future[Boolean]

  def invalidarToken(token: String): Future[Int]

}
