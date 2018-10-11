package portal.transaccional.autenticacion.service.drivers.usuarioIndividual

import java.sql.Timestamp

import co.com.alianza.commons.enumerations.TiposCliente._
import co.com.alianza.persistence.entities.Usuario
import portal.transaccional.autenticacion.service.web.autenticacion.UsuarioGenRequest

import scala.concurrent.Future

/**
 * Created by hernando on 25/07/16.
 */
trait UsuarioRepository {

  def getById(idUsuario: Int): Future[Option[Usuario]]

  def getByIdentificacion(numeroIdentificacion: String, tipoIdentificacion: Int): Future[Usuario]

  def getByToken(token: String): Future[Option[Usuario]]

  def validarEstado(estado: Int): Future[Boolean]

  def validarContrasena(contrasena: String, usuario: Usuario, reintentosErroneos: Int): Future[Boolean]

  def validarContrasena(contrasena: String, usuario: Usuario): Future[Boolean]

  def actualizarToken(numeroIdentificacion: String, token: String): Future[Int]

  def actualizarEstado(idUsuario: Int, estado: Int): Future[Int]

  def actualizarIngresosErroneosUsuario(idUsuario: Int, numeroIntentos: Int): Future[Int]

  def actualizarIp(numeroIdentificacion: String, ip: String): Future[Int]

  def actualizarFechaIngreso(idUsuario: Int, fechaActual: Timestamp): Future[Int]

  def actualizarContrasena(idUsuario: Int, contrasena: String): Future[Int]

  def validarCaducidadContrasena(tipoCliente: TiposCliente, usuario: Usuario, dias: Int): Future[Boolean]

  def invalidarToken(token: String): Future[Int]

  def validarUsuario(usuarioOption: Option[Usuario]): Future[Usuario]

  def getByUsuario(usuario: UsuarioGenRequest): Future[Usuario]

  /**
   * Crea un usuario si no existe en base de datos
   * @param usuario Usuario a crear
   * @return
   */
  def createIfNotExist(usuario: Usuario): Future[Int]

}
