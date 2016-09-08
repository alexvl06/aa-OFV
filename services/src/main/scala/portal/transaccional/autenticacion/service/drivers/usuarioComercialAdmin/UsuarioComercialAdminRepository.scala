package portal.transaccional.autenticacion.service.drivers.usuarioComercialAdmin

import java.sql.Timestamp

import co.com.alianza.commons.enumerations.TiposCliente.TiposCliente
import co.com.alianza.persistence.entities.{ UsuarioComercial, UsuarioComercialAdmin }

import scala.concurrent.Future

/**
 * Created by hernando on 8/08/16.
 */
trait UsuarioComercialAdminRepository {

  def obtenerUsuario(usuario: String): Future[UsuarioComercialAdmin]

  def getByToken(token: String): Future[Option[UsuarioComercialAdmin]]

  def crearToken(idUsuario: Int, token: String): Future[Int]

  def eliminarToken(token: String): Future[Int]

  def actualizarIp(idUsuario: Int, ip: String): Future[Int]

  def actualizarFechaIngreso(idUsuario: Int, fechaActual: Timestamp): Future[Int]

  def validarContrasena(contrasena: String, usuario: UsuarioComercialAdmin): Future[Boolean]

  def crearUsuario(tipoCliente: TiposCliente, contrasena: String, usuario: String, nombre: String, correo: String): Future[Int]

}
