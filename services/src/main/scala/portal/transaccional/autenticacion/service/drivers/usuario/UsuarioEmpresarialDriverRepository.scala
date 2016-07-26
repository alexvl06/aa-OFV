package portal.transaccional.autenticacion.service.drivers.usuario

import co.com.alianza.exceptions.ValidacionException
import co.com.alianza.persistence.entities.UsuarioEmpresarial
import co.com.alianza.util.clave.Crypto
import enumerations.{ AppendPasswordUser, EstadosEmpresaEnum }
import portal.transaccional.fiduciaria.autenticacion.storage.daos.portal.UsuarioEmpresarialDAOs

import scala.concurrent.{ Future, ExecutionContext }

/**
 * Created by hernando on 26/07/16.
 */
case class UsuarioEmpresarialDriverRepository(usuarioDAO: UsuarioEmpresarialDAOs)(implicit val ex: ExecutionContext) extends UsuarioEmpresarialRepository {

  def getByIdentityAndUser(identificacion: String, usuario: String): Future[Option[UsuarioEmpresarial]] = {
    usuarioDAO.getByIdentityAndUser(identificacion, usuario)
  }

  ///////////////////////////////////// validaciones ///////////////////////////////////////

  /**
   * Validacion de existencia, estado y contrasena
   * @param usuarioOption
   * @param contrasena
   * @return
   */
  def validarUsuario(usuarioOption: Option[UsuarioEmpresarial], contrasena: String): Future[UsuarioEmpresarial] = {
    for {
      usuario <- validarExiste(usuarioOption)
      estado <- validarEstado(usuario)
      contrasena <- validarContrasena(contrasena, usuario)
    } yield usuario
  }

  /**
   * Validar si el usuario existe
   * @param usuarioOption
   * @return
   */
  def validarExiste(usuarioOption: Option[UsuarioEmpresarial]): Future[UsuarioEmpresarial] = {
    usuarioOption match {
      case Some(usuario: UsuarioEmpresarial) => Future.successful(usuario)
      case _ => Future.failed(ValidacionException("401.3", "Error Credenciales"))
    }
  }

  /**
   * Validar los estados del usuario
   * @param usuario
   * @return
   */
  def validarEstado(usuario: UsuarioEmpresarial): Future[Boolean] = {
    val estado = usuario.estado
    if (estado == EstadosEmpresaEnum.bloqueContraseña.id)
      Future.failed(ValidacionException("401.8", "Usuario Bloqueado"))
    else if (estado == EstadosEmpresaEnum.pendienteActivacion.id)
      Future.failed(ValidacionException("401.10", "Usuario Bloqueado"))
    else if (estado == EstadosEmpresaEnum.pendienteReiniciarContrasena.id)
      Future.failed(ValidacionException("401.12", "Usuario Bloqueado"))
    else if (estado == EstadosEmpresaEnum.bloqueadoPorAdmin.id)
      Future.failed(ValidacionException("401.14", "Usuario Desactivado"))
    else Future.successful(true)
  }

  /**
   * Validar la contrasena
   * @param contrasena
   * @param usuario
   * @return
   */
  def validarContrasena(contrasena: String, usuario: UsuarioEmpresarial): Future[Boolean] = {
    val hash = Crypto.hashSha512(contrasena.concat(AppendPasswordUser.appendUsuariosFiducia), usuario.id)
    if (hash.contentEquals(usuario.contrasena.get)) Future.successful(true)
    else Future.failed(ValidacionException("401.3", "Error Credenciales"))
  }

}
