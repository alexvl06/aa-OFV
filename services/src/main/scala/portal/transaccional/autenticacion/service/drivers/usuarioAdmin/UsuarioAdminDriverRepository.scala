package portal.transaccional.autenticacion.service.drivers.usuarioAdmin

import java.sql.Timestamp
import java.util.Date

import co.com.alianza.commons.enumerations.TiposCliente.TiposCliente
import co.com.alianza.exceptions.ValidacionException
import co.com.alianza.persistence.entities.UsuarioEmpresarialAdmin
import co.com.alianza.util.clave.Crypto
import co.com.alianza.util.token.Token
import enumerations.{ AppendPasswordUser, EstadosEmpresaEnum, EstadosUsuarioEnum }
import org.joda.time.DateTime
import portal.transaccional.fiduciaria.autenticacion.storage.daos.portal.UsuarioEmpresarialAdminDAOs

import scala.concurrent.{ ExecutionContext, Future }

/**
 * Created by hernando on 26/07/16.
 */
case class UsuarioAdminDriverRepository(usuarioDAO: UsuarioEmpresarialAdminDAOs)(implicit val ex: ExecutionContext)
    extends UsuarioAdminRepository {

  def getById(id: Int): Future[UsuarioEmpresarialAdmin] = {
    usuarioDAO.getById(id) flatMap {
      (usuarioOption: Option[UsuarioEmpresarialAdmin]) =>
        usuarioOption match {
          case Some(admin: UsuarioEmpresarialAdmin) => Future.successful(admin)
          case _ => Future.failed(ValidacionException("401.3", "Error admin no existe"))
        }
    }
  }

  def getByIdentityAndUser(identificacion: String, usuario: String): Future[Option[UsuarioEmpresarialAdmin]] = {
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

  def actualizarEstado(idUsuario: Int, estado: Int): Future[Int] = {
    usuarioDAO.updateStateById(idUsuario, estado)
  }

  def actualizarInfoUsuario(usuario: UsuarioEmpresarialAdmin, ip: String): Future[Int] = {
    for {
      intentos <- usuarioDAO.updateIncorrectEntries(usuario.id, 0)
      ip <- usuarioDAO.updateLastIp(usuario.id, ip)
      fecha <- usuarioDAO.updateLastDate(usuario.id, new Timestamp((new Date).getTime))
    } yield fecha
  }

  /**
   * Guardar contrasena
   *
   * @param idUsuario
   * @param contrasena
   */
  def actualizarContrasena(idUsuario: Int, contrasena: String): Future[Int] = {
    for {
      cambiar <- usuarioDAO.updatePassword(idUsuario, contrasena)
      _ <- usuarioDAO.updateStateById(idUsuario, EstadosEmpresaEnum.activo.id)
      _ <- usuarioDAO.updateUpdateDate(idUsuario, new Timestamp(new Date().getTime))
      _ <- usuarioDAO.updateIncorrectEntries(idUsuario, 0)
    } yield cambiar
  }

  /**
   * Invalidar el token al usuario
   * @param token
   * @return
   */
  def invalidarToken(token: String): Future[Int] = {
    usuarioDAO.deleteToken(token)
  }

  /////////////////////////////// validaciones //////////////////////////////////

  /**
   * Validacion de existencia, estado y contrasena
   * @param usuario
   * @param contrasena
   * @return
   */
  def validarUsuario(usuario: UsuarioEmpresarialAdmin, contrasena: String, reintentosErroneos: Int): Future[Boolean] = {
    for {
      estado <- validarEstado(usuario)
      contrasena <- validarContrasena(contrasena, usuario, reintentosErroneos)
    } yield contrasena
  }

  def validacionBloqueoAdmin(usuario: UsuarioEmpresarialAdmin): Future[Boolean] = {
    val bloqueoPorAdmin: Int = EstadosEmpresaEnum.bloqueadoPorAdmin.id
    usuario.estado match {
      case `bloqueoPorAdmin` => Future.failed(ValidacionException("409.13", "Usuario Bloqueado Admin"))
      case _ => Future.successful(true)
    }
  }

  def validacionExisteAdminActivo(usuario: UsuarioEmpresarialAdmin): Future[Boolean] = {
    val activo: Int = EstadosEmpresaEnum.activo.id
    usuario.estado match {
      case `activo` => Future.successful(true)
      case _ => for {
        existe <- usuarioDAO.existAdminActive(usuario.identificacion)
        validacion <- if (existe) Future.failed(ValidacionException("409.12", "Usuario Admin Activo Existe")) else Future.successful(existe)
      } yield validacion
    }
  }

  /**
   * Validar los estados del usuario
   * @param usuario
   * @return
   */
  def validarEstado(usuario: UsuarioEmpresarialAdmin): Future[Boolean] = {
    val estado = usuario.estado
    if (estado == EstadosEmpresaEnum.bloqueContrase??a.id) {
      Future.failed(ValidacionException("401.8", "Usuario Bloqueado"))
    } else if (estado == EstadosEmpresaEnum.pendienteActivacion.id) {
      Future.failed(ValidacionException("401.10", "Usuario Bloqueado"))
    } else if (estado == EstadosEmpresaEnum.pendienteReiniciarContrasena.id) {
      Future.failed(ValidacionException("401.12", "Usuario Bloqueado"))
    } else if (estado == EstadosEmpresaEnum.bloqueadoPorAdmin.id) {
      Future.failed(ValidacionException("401.14", "Usuario Desactivado"))
    } else {
      Future.successful(true)
    }
  }

  /**
   * Validar la contrasena
   * @param contrasena
   * @param usuario
   * @return
   */
  def validarContrasena(contrasena: String, usuario: UsuarioEmpresarialAdmin, reintentosErroneos: Int): Future[Boolean] = {
    val hash = Crypto.hashSha512(contrasena.concat(AppendPasswordUser.appendUsuariosFiducia), usuario.id)
    if (hash.contentEquals(usuario.contrasena.get)) {
      Future.successful(true)
    } else {
      //si las contrase??as no concuerdan:
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
      usuarioDAO.updateStateById(idUsuario, EstadosUsuarioEnum.bloqueContrase??a.id).map(_ => true)
    } else {
      Future.successful(true)
    }
  }

  /**
   * Valida la fecha de caducidad de la contrase??a de un usuario
   * @param tipoCliente
   * @param usuario
   * @param dias
   * @return
   */
  def validarCaducidadContrasena(tipoCliente: TiposCliente, usuario: UsuarioEmpresarialAdmin, dias: Int): Future[Boolean] = {
    if (new DateTime().isAfter(new DateTime(usuario.fechaActualizacion.getTime).plusDays(dias))) {
      val token: String = Token.generarTokenCaducidadContrasena(tipoCliente, usuario.id)
      Future.failed(ValidacionException("401.9", token))
    } else {
      Future.successful(true)
    }
  }

}
