package portal.transaccional.autenticacion.service.drivers.usuarioAgente

import java.sql.Timestamp

import co.com.alianza.commons.enumerations.TiposCliente._
import co.com.alianza.persistence.entities.Agente

import scala.concurrent.Future

/**
 * Created by hernando on 26/07/16.
 */
trait UsuarioEmpresarialRepository {

  def getByIdentityAndUser(identificacion: String, usuario: String): Future[Option[Agente]]

  def actualizarToken(idUsuario: Int, token: String): Future[Int]

  def actualizarIngresosErroneosUsuario(idUsuario: Int, numeroIntentos: Int): Future[Int]

  def actualizarIp(idUsuario: Int, ip: String): Future[Int]

  def actualizarFechaIngreso(idUsuario: Int, fechaActual: Timestamp): Future[Int]

  def actualizarInfoUsuario(usuario: Agente, ip: String): Future[Int]

  def validarUsuario(usuario: Agente, contrasena: String, reintentosErroneos: Int): Future[Boolean]

  def validarEstado(usuario: Agente): Future[Boolean]

  def validarContrasena(contrasena: String, usuario: Agente, reintentosErroneos: Int): Future[Boolean]

  def validarCaducidadContrasena(tipoCliente: TiposCliente, usuario: Agente, dias: Int): Future[Boolean]

  def invalidarToken(token: String): Future[Int]

}
