package portal.transaccional.autenticacion.service.drivers.usuarioComercial

import java.sql.Timestamp

import co.com.alianza.persistence.entities.UsuarioComercial

import scala.concurrent.Future

/**
 * Created by alexandra on 2016
 */
trait UsuarioComercialRepository {

  def getUser(usuario: String): Future[UsuarioComercial]

  def getByUser(usuario: String): Future[Option[UsuarioComercial]]

  def getByToken(token: String): Future[Option[UsuarioComercial]]

  def crearToken(idUsuario: Int, token: String): Future[Int]

  def eliminarToken(token: String): Future[Int]

  def validarEstado(estado: Int): Future[Boolean]

  def validarContrasena(contrasenaIngresada: String, usuario: UsuarioComercial, contrasenaValida: String): Future[Boolean]

  def invalidarToken(token: String): Future[Int]

  def updateIpFecha(nombreUsuario: String, ip: String): Future[Int]

  def existeUsuario(nombreUsuario: String): Future[Boolean]

  def crearUsuario(nombreUsuario: String, ip: String): Future[Int]

}
