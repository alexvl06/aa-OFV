package portal.transaccional.autenticacion.service.drivers.usuarioComercial

import java.sql.Timestamp

import co.com.alianza.persistence.entities.UsuarioComercial

import scala.concurrent.Future

/**
 * Created by alexandra on 5/08/16.
 */
trait UsuarioComercialRepository {

  def getByUser(usuario: String): Future[UsuarioComercial]

  def getByToken(token: String): Future[Option[UsuarioComercial]]

  def actualizarToken(idUsuario: Int, token: String): Future[Int]

  def actualizarIp(numeroIdentificacion: String, ip: String): Future[Int]

  def actualizarFechaIngreso(numeroIdentificacion: String, fechaActual: Timestamp): Future[Int]

  def validarEstado(estado: Int): Future[Boolean]

  def validarContrasena(contrasenaIngresada: String, usuario: UsuarioComercial, contrasenaValida: String): Future[Boolean]

  def invalidarToken(token: String): Future[Int]

}
