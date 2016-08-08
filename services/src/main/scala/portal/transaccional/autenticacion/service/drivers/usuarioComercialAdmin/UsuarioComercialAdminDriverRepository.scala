package portal.transaccional.autenticacion.service.drivers.usuarioComercialAdmin

import java.sql.Timestamp

import co.com.alianza.exceptions.ValidacionException
import co.com.alianza.persistence.entities.{UsuarioComercial, UsuarioComercialAdmin}
import co.com.alianza.util.clave.Crypto
import enumerations.AppendPasswordUser
import portal.transaccional.fiduciaria.autenticacion.storage.daos.portal.UsuarioComercialAdminDAOs

import scala.concurrent.Future

/**
 * Created by alexandra on 5/08/16.
 */
case class UsuarioComercialAdminDriverRepository(usuarioDAO: UsuarioComercialAdminDAOs){

  def getByUser(usuario: String): Future[UsuarioComercial]

  def getByToken(token: String): Future[Option[UsuarioComercial]]

  def actualizarToken(idUsuario: Int, token: String): Future[Int]

  def actualizarIp(idUsuario: Int, ip: String): Future[Int]

  def actualizarFechaIngreso(idUsuario: Int, fechaActual: Timestamp): Future[Int]

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

}
