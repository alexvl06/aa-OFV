package portal.transaccional.autenticacion.service.drivers.usuarioAgente

import java.sql.Timestamp

import co.com.alianza.commons.enumerations.TiposCliente._
import co.com.alianza.persistence.entities.UsuarioAgenteEmpresarial

import scala.concurrent.Future

/**
 * Created by hernando on 26/07/16.
 */
trait UsuarioAgenteEmpresarialRepository {

  def getById(idUsuario: Int): Future[Option[UsuarioAgenteEmpresarial]]

  def getByIdentityAndUser(identificacion: String, usuario: String): Future[Option[UsuarioAgenteEmpresarial]]

  def actualizarToken(idUsuario: Int, token: String): Future[Int]

  def actualizarIngresosErroneosUsuario(idUsuario: Int, numeroIntentos: Int): Future[Int]

  def actualizarIp(idUsuario: Int, ip: String): Future[Int]

  def actualizarFechaIngreso(idUsuario: Int, fechaActual: Timestamp): Future[Int]

  def actualizarInfoUsuario(usuario: UsuarioAgenteEmpresarial, ip: String): Future[Int]

  def actualizarContrasena(idUsuario: Int, contrasena: String): Future[Int]

  def validacionBloqueoAdmin(usuario: UsuarioAgenteEmpresarial): Future[Boolean]

  def validarUsuario(usuarioOption: Option[UsuarioAgenteEmpresarial]): Future[UsuarioAgenteEmpresarial]

  def validarUsuario(usuario: UsuarioAgenteEmpresarial, contrasena: String, reintentosErroneos: Int): Future[Boolean]

  def validarEstado(usuario: UsuarioAgenteEmpresarial): Future[Boolean]

  def validarContrasena(contrasena: String, usuario: UsuarioAgenteEmpresarial, reintentosErroneos: Int): Future[Boolean]

  def validarCaducidadContrasena(tipoCliente: TiposCliente, usuario: UsuarioAgenteEmpresarial, dias: Int): Future[Boolean]

  def invalidarToken(token: String): Future[Int]

}
