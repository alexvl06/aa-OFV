package portal.transaccional.autenticacion.service.drivers.usuarioComercialAdmin

import java.sql.Timestamp

import co.com.alianza.persistence.entities.{UsuarioComercialAdmin, UsuarioComercial}

import scala.concurrent.Future

/**
 * Created by hernando on 8/08/16.
 */
trait UsuarioComercialAdminRepository {

  def getByUser(usuario: String): Future[UsuarioComercial]

  def getByToken(token: String): Future[Option[UsuarioComercial]]

  def actualizarToken(idUsuario: Int, token: String): Future[Int]

  def actualizarIp(idUsuario: Int, ip: String): Future[Int]

  def actualizarFechaIngreso(idUsuario: Int, fechaActual: Timestamp): Future[Int]

  def validarContrasena(contrasena: String, usuario: UsuarioComercialAdmin): Future[Boolean]

}
