package portal.transaccional.autenticacion.service.drivers.usuario

import java.sql.Timestamp
import java.util.Date

import co.com.alianza.commons.enumerations.TiposCliente._
import co.com.alianza.exceptions.ValidacionException
import co.com.alianza.persistence.entities.UsuarioEmpresarial
import co.com.alianza.util.clave.Crypto
import co.com.alianza.util.token.Token
import enumerations.{ EstadosUsuarioEnum, AppendPasswordUser, EstadosEmpresaEnum }
import org.joda.time.DateTime
import portal.transaccional.fiduciaria.autenticacion.storage.daos.portal.UsuarioEmpresarialDAOs

import scala.concurrent.{ ExecutionContext, Future }

/**
 * Created by hernando on 26/07/16.
 */
case class UsuarioEmpresarialDriverRepository(usuarioDAO: UsuarioEmpresarialDAOs)(implicit val ex: ExecutionContext) extends UsuarioEmpresarialRepository {

  def getByIdentityAndUser(identificacion: String, usuario: String): Future[Option[UsuarioEmpresarial]] = {
    usuarioDAO.getByIdentityAndUser(identificacion, usuario)
  }

  /**
   * Asociar el token al usuario
   * @param idUsuario
   * @param token
   * @return
   */
  def actualizarToken(idUsuario: Int, token: String): Future[Int] = {
    usuarioDAO.createToken(idUsuario, token)
  }

  /**
   * Actualiza los ingresos erroneos de un usuario al numero especificado por parametro
   * @param idUsuario
   * @param numeroIntentos
   * @return
   */
  def actualizarIngresosErroneosUsuario(idUsuario: Int, numeroIntentos: Int): Future[Int] = {
    usuarioDAO.updateIncorrectEntries(idUsuario, numeroIntentos)
  }

  /**
   * Actualizar ultima ip
   * @param idUsuario
   * @param ip
   * @return
   */
  def actualizarIp(idUsuario: Int, ip: String): Future[Int] = {
    usuarioDAO.updateLastIp(idUsuario, ip)
  }

  /**
   * Actualizar fecha ingreso
   * @param idUsuario
   * @param fechaActual
   * @return
   */
  def actualizarFechaIngreso(idUsuario: Int, fechaActual: Timestamp): Future[Int] = {
    usuarioDAO.updateLastDate(idUsuario, fechaActual)
  }

  def actualizarInfoUsuario(usuario: UsuarioEmpresarial, ip: String): Future[Int] = {
    for {
      intentos <- usuarioDAO.updateIncorrectEntries(usuario.id, 0)
      ip <- usuarioDAO.updateLastIp(usuario.id, ip)
      fecha <- usuarioDAO.updateLastDate(usuario.id, new Timestamp((new Date).getTime))
    } yield fecha
  }

  /////////////////////////////// validaciones //////////////////////////////////

  /**
   * Validacion de existencia, estado y contrasena
   * @param usuarioOption
   * @param contrasena
   * @return
   */
  def validarUsuario(usuarioOption: Option[UsuarioEmpresarial], contrasena: String, reintentosErroneos: Int): Future[UsuarioEmpresarial] = {
    for {
      usuario <- validarExiste(usuarioOption)
      estado <- validarEstado(usuario)
      contrasena <- validarContrasena(contrasena, usuario, reintentosErroneos)
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
  def validarContrasena(contrasena: String, usuario: UsuarioEmpresarial, reintentosErroneos: Int): Future[Boolean] = {
    val hash = Crypto.hashSha512(contrasena.concat(AppendPasswordUser.appendUsuariosFiducia), usuario.id)
    if (hash.contentEquals(usuario.contrasena.get)) {
      Future.successful(true)
    } else {
      //si las contraseñas no concuerdan:
      //1. actualizar ingresos erroneos
      //2. bloquear usuario si es necesario
      val reintentos: Int = usuario.numeroIngresosErroneos + 1
      for {
        actualizarIngresos <- actualizarIngresosErroneosUsuario(usuario.id, reintentos)
        bloquear <- validarBloqueoUsuario(usuario.id, reintentos, reintentosErroneos)
        error <- Future.failed(ValidacionException("401.3", "Error Credenciales"))
      } yield error
    }
  }

  /**
   * Bloquea el usuario si se incumple la regla por parametro
   * @param idUsuario
   * @param reintentos
   * @param reintentosErroneos
   * @return
   */
  private def validarBloqueoUsuario(idUsuario: Int, reintentos: Int, reintentosErroneos: Int): Future[Boolean] = {
    if (reintentos >= reintentosErroneos) {
      usuarioDAO.updateStateById(idUsuario, EstadosUsuarioEnum.bloqueContraseña.id).map(_ => true)
    } else Future.successful(true)
  }

  /**
   * Valida la fecha de caducidad de la contraseña de un usuario
   * @param tipoCliente
   * @param usuario
   * @param dias
   * @return
   */
  def validarCaducidadContrasena(tipoCliente: TiposCliente, usuario: UsuarioEmpresarial, dias: Int): Future[Boolean] = {
    if (new DateTime().isAfter(new DateTime(usuario.fechaActualizacion.getTime).plusDays(dias))) {
      val token: String = Token.generarTokenCaducidadContrasena(tipoCliente, usuario.id)
      Future.failed(ValidacionException("401.9", token))
    } else {
      Future.successful(true)
    }
  }

}
