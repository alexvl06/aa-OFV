package portal.transaccional.autenticacion.service.drivers.usuarioIndividual

import java.sql.Timestamp

import co.com.alianza.commons.enumerations.TiposCliente._
import co.com.alianza.persistence.entities.Usuario

import scala.concurrent.Future

/**
 * Created by hernando on 25/07/16.
 */
trait UsuarioRepository {

  def getByIdentificacion(numeroIdentificacion: String): Future[Usuario]

  def getByToken(token: String): Future[Option[Usuario]]

  def validarEstado(estado: Int): Future[Boolean]

  def validarContrasena(contrasena: String, usuario: Usuario, reintentosErroneos: Int): Future[Boolean]

  def actualizarToken(numeroIdentificacion: String, token: String): Future[Int]

  def actualizarIngresosErroneosUsuario(idUsuario: Int, numeroIntentos: Int): Future[Int]

  def actualizarIp(numeroIdentificacion: String, ip: String): Future[Int]

  def actualizarFechaIngreso(idUsuario: Int, fechaActual: Timestamp): Future[Int]

  def actualizarContrasena(idUsuario: Int, contrasena: String): Future[Int]

  def validarCaducidadContrasena(tipoCliente: TiposCliente, usuario: Usuario, dias: Int): Future[Boolean]

  def invalidarToken(token: String): Future[Int]

}
