package portal.transaccional.autenticacion.service.drivers.usuarioAgente

import java.sql.Timestamp

import co.com.alianza.commons.enumerations.TiposCliente._
import co.com.alianza.persistence.entities.{UsuarioAgente, UsuarioEmpresarial}

import scala.concurrent.Future

/**
 * Created by hernando on 2016
 */
trait UsuarioEmpresarialRepository[E <: UsuarioAgente] {

  def getById(idUsuario: Int): Future[Option[UsuarioAgente]]

  def actualizarInfoUsuario(usuario: UsuarioAgente, ip: String): Future[Int]

  def getByIdentityAndUser(identificacion: String, usuario: String): Future[Option[UsuarioAgente]]

  def actualizarToken(idUsuario: Int, token: String): Future[Int]

  def actualizarIngresosErroneosUsuario(idUsuario: Int, numeroIntentos: Int): Future[Int]

  def actualizarIp(idUsuario: Int, ip: String): Future[Int]

  def actualizarFechaIngreso(idUsuario: Int, fechaActual: Timestamp): Future[Int]

  def validarUsuario(usuario: UsuarioAgente, contrasena: String, reintentosErroneos: Int): Future[Boolean]

  def validarEstado(usuario: UsuarioAgente): Future[Boolean]

  def validarContrasena(contrasena: String, usuario: UsuarioAgente, reintentosErroneos: Int): Future[Boolean]

  def validarCaducidadContrasena(tipoCliente: TiposCliente, usuario: UsuarioAgente, dias: Int): Future[Boolean]

  def invalidarToken(token: String): Future[Int]

}
