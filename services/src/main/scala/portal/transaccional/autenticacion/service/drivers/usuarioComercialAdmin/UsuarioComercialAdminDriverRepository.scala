package portal.transaccional.autenticacion.service.drivers.usuarioComercialAdmin

import java.sql.Timestamp
import java.util.Date

import co.com.alianza.commons.enumerations.TiposCliente
import co.com.alianza.commons.enumerations.TiposCliente._
import co.com.alianza.exceptions.ValidacionException
import co.com.alianza.persistence.entities.UsuarioComercialAdmin
import co.com.alianza.util.clave.Crypto
import enumerations.AppendPasswordUser
import portal.transaccional.fiduciaria.autenticacion.storage.daos.portal.UsuarioComercialAdminDAOs

import scala.concurrent.{ ExecutionContext, Future }

/**
 * Created by alexandra on 5/08/16.
 */
case class UsuarioComercialAdminDriverRepository(usuarioDAO: UsuarioComercialAdminDAOs)(implicit val ex: ExecutionContext)
    extends UsuarioComercialAdminRepository {

  def obtenerUsuario(usuario: String): Future[UsuarioComercialAdmin] = {
    usuarioDAO.getByUser(usuario) flatMap {
      (usuarioOption: Option[UsuarioComercialAdmin]) =>
        usuarioOption match {
          case Some(usuario: UsuarioComercialAdmin) => Future.successful(usuario)
          case _ => Future.failed(ValidacionException("401.3", "Error usuario no existe"))
        }
    }
  }

  def crearToken(idUsuario: Int, token: String): Future[Int] = usuarioDAO.createToken(idUsuario, token)

  def eliminarToken(token: String): Future[Int] = usuarioDAO.deleteToken(token)

  def actualizarIp(idUsuario: Int, ip: String): Future[Int] = usuarioDAO.updateLastIp(idUsuario, ip)

  def actualizarFechaIngreso(idUsuario: Int, fechaActual: Timestamp): Future[Int] = usuarioDAO.updateLastDate(idUsuario, fechaActual)

  /**
   * Validar la contrasena
   * @param contrasena
   * @param usuario
   * @return
   */
  def validarContrasena(contrasena: String, usuario: UsuarioComercialAdmin): Future[Boolean] = {
    val hash = Crypto.hashSha512(contrasena.concat(AppendPasswordUser.appendUsuariosFiducia), usuario.id)
    if (hash.contentEquals(usuario.contrasena.get)) {
      Future.successful(true)
    } else {
      Future.failed(ValidacionException("401.3", "Error Credenciales"))
    }
  }

  def crearUsuario(tipoCliente: TiposCliente, contrasena: String, usuario: String, nombre: String, correo: String): Future[Int] = {
    val fechaActual = new Timestamp((new Date).getTime)
    for {
      validarPeticion <- validarTipoCliente(tipoCliente)
      obtenerUsuario <- usuarioDAO.getByUser(usuario)
      validar <- validarUsuario(obtenerUsuario)
      obtenerCorreo <- usuarioDAO.getByEmail(correo)
      validarCorreo <- validarCorreo(obtenerCorreo)
      crearUsuario <- usuarioDAO.create(UsuarioComercialAdmin(0, correo, usuario, None, None, None, None, fechaActual, Some(nombre)))
      crearContrasena <- crearContrasena(contrasena, crearUsuario)
    } yield crearUsuario
  }

  private def crearContrasena(contrasena: String, id: Int) = {
    val passwordAppend = contrasena.concat(AppendPasswordUser.appendUsuariosFiducia)
    val uContrasena = Crypto.hashSha512(passwordAppend, id)
    usuarioDAO.updatePassword(id, uContrasena)
  }

  private def validarUsuario(usuario: Option[UsuarioComercialAdmin]): Future[Boolean] = {
    usuario match {
      case None => Future.successful(true)
      case Some(value) => Future.failed(ValidacionException("401.6", "Error usuario ya existe"))
    }
  }

  private def validarCorreo(usuario: Option[UsuarioComercialAdmin]): Future[Boolean] = {
    usuario match {
      case None => Future.successful(true)
      case Some(value) => Future.failed(ValidacionException("401.7", "Error correo ya existe"))
    }
  }

  private def validarTipoCliente(tipoCliente: TiposCliente): Future[Boolean] = {
    tipoCliente match {
      case TiposCliente.comercialAdmin => Future.successful(true)
      case _ => Future.failed(ValidacionException("401.8", "Error tipo cliente no valido"))
    }
  }
}
