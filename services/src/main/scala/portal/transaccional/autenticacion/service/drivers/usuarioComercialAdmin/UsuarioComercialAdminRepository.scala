package portal.transaccional.autenticacion.service.drivers.usuarioComercialAdmin

import java.sql.Timestamp

import co.com.alianza.commons.enumerations.TiposCliente.TiposCliente
import co.com.alianza.infrastructure.dto.security.UsuarioAuth
import co.com.alianza.persistence.entities.{ Empresa, UsuarioComercial, UsuarioComercialAdmin }

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

  def actualizarContrasena(usuario: UsuarioAuth, contrasenaActual: String, contrasenaNueva: String): Future[Int]

  def validarEmpresa(identificacion: String): Future[Empresa]

}
