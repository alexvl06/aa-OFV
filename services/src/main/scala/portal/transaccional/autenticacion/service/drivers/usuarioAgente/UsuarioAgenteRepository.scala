package portal.transaccional.autenticacion.service.drivers.usuarioAgente

import java.sql.Timestamp

import co.com.alianza.commons.enumerations.TiposCliente._
import co.com.alianza.persistence.entities.UsuarioAgente

import scala.concurrent.Future

/**
 * Created by hernando on 26/07/16.
 */
trait UsuarioAgenteRepository {

  def getById(idUsuario: Int): Future[Option[UsuarioAgente]]

  def getByIdentityAndUser(identificacion: String, usuario: String): Future[Option[UsuarioAgente]]

  def actualizarToken(idUsuario: Int, token: String): Future[Int]

  def actualizarIngresosErroneosUsuario(idUsuario: Int, numeroIntentos: Int): Future[Int]

  def actualizarIp(idUsuario: Int, ip: String): Future[Int]

  def actualizarFechaIngreso(idUsuario: Int, fechaActual: Timestamp): Future[Int]

  def actualizarInfoUsuario(usuario: UsuarioAgente, ip: String): Future[Int]

  def actualizarContrasena(idUsuario: Int, contrasena: String): Future[Int]

  def validacionBloqueoAdmin(usuario: UsuarioAgente): Future[Boolean]

  def validarUsuario(usuarioOption: Option[UsuarioAgente]): Future[UsuarioAgente]

  def validarUsuario(usuario: UsuarioAgente, contrasena: String, reintentosErroneos: Int): Future[Boolean]

  def validarEstado(usuario: UsuarioAgente): Future[Boolean]

  def validarContrasena(contrasena: String, usuario: UsuarioAgente, reintentosErroneos: Int): Future[Boolean]

  def validarCaducidadContrasena(tipoCliente: TiposCliente, usuario: UsuarioAgente, dias: Int): Future[Boolean]

  def invalidarToken(token: String): Future[Int]

}
