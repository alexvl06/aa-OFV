package portal.transaccional.autenticacion.service.drivers.usuarioAgente

import java.sql.Timestamp
import java.util.Date

import co.com.alianza.commons.enumerations.TiposCliente._
import co.com.alianza.exceptions.ValidacionException
import co.com.alianza.persistence.entities.{ UsuarioAgente, UsuarioAgenteTable }
import co.com.alianza.util.clave.Crypto
import co.com.alianza.util.token.Token
import enumerations.{ AppendPasswordUser, EstadosEmpresaEnum, EstadosUsuarioEnum }
import org.joda.time.DateTime
import portal.transaccional.fiduciaria.autenticacion.storage.daos.portal.UsuarioAgenteDAOs

import scala.concurrent.{ ExecutionContext, Future }

/**
 * Created by s4n on 2016
 */
abstract class UsuarioEmpresarialRepositoryG[T <: UsuarioAgenteTable[E], E <: UsuarioAgente](usuarioDAO: UsuarioAgenteDAOs[T, E])
    extends UsuarioEmpresarialRepository[E] {

  implicit val ex: ExecutionContext

  def getById(idUsuario: Int): Future[Option[E]] = usuarioDAO.getById(idUsuario)

  def getByIdentityAndUser(identificacion: String, usuario: String): Future[Option[E]] = {
    usuarioDAO.getByIdentityAndUser(identificacion, usuario)
  }

  def invalidarToken(token: String) = usuarioDAO.deleteToken(token)

  /**
   * Asociar el token al usuario
   * @param idUsuario
   * @param token
   * @return
   */
  def actualizarToken(idUsuario: Int, token: String): Future[Int] = usuarioDAO.updateToken(idUsuario, token)

  /**
   * Actualiza los ingresos erroneos de un usuario al numero especificado por parametro
   * @param idUsuario
   * @param numeroIntentos
   * @return
   */
  def actualizarIngresosErroneosUsuario(idUsuario: Int, numeroIntentos: Int): Future[Int] = usuarioDAO.updateIncorrectEntries(idUsuario, numeroIntentos)

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
    usuarioDAO.updateLastEntryDate(idUsuario, fechaActual)
  }

  def actualizarFechaActualizacion(idUsuario: Int, fechaActual: Timestamp): Future[Int] = {
    usuarioDAO.updateUpdateDate(idUsuario, fechaActual)
  }

  /**
   * Guardar contrasena
   *
   * @param idUsuario
   * @param contrasena
   */
  def actualizarContrasena(idUsuario: Int, contrasena: String): Future[Int] = {
    for {
      cambiar <- usuarioDAO.updatePasswordById(idUsuario, contrasena)
      _ <- usuarioDAO.updateStateById(idUsuario, EstadosEmpresaEnum.activo.id)
      _ <- usuarioDAO.updateUpdateDate(idUsuario, new Timestamp(new Date().getTime))
      _ <- usuarioDAO.updateIncorrectEntries(idUsuario, 0)
    } yield cambiar
  }

  def actualizarEstado(idUsuario: Int, estado: EstadosEmpresaEnum.estadoEmpresa): Future[Int] = {
    usuarioDAO.updateStateById(idUsuario, estado.id)
  }

  def actualizarInfoUsuario(usuario: UsuarioAgente, ip: String): Future[Int] = {
    for {
      intentos <- usuarioDAO.updateIncorrectEntries(usuario.id, 0)
      ip <- usuarioDAO.updateLastIp(usuario.id, ip)
      fecha <- usuarioDAO.updateLastEntryDate(usuario.id, new Timestamp((new Date).getTime))
    } yield fecha
  }

  /////////////////////////////// validaciones //////////////////////////////////

  def validarBloqueoAdmin(usuario: UsuarioAgente): Future[Boolean] = {
    val bloqueoPorAdmin: Int = EstadosEmpresaEnum.bloqueadoPorAdmin.id
    usuario.estado match {
      case `bloqueoPorAdmin` => Future.failed(ValidacionException("409.13", "Usuario Bloqueado Admin"))
      case _ => Future.successful(true)
    }
  }

  def validarUsuario(usuarioOption: Option[UsuarioAgente]): Future[UsuarioAgente] = {
    usuarioOption match {
      case Some(usuario) => Future.successful(usuario)
      case _ => Future.failed(ValidacionException("409.01", "No existe usuario"))
    }
  }

  /**
   * Validacion de existencia, estado y contrasena
   * @param usuario
   * @param contrasena
   * @return
   */
  def validarUsuario(usuario: UsuarioAgente, contrasena: String, reintentosErroneos: Int): Future[Boolean] = {
    for {
      estado <- validarEstado(usuario)
      contrasena <- validarContrasena(contrasena, usuario, reintentosErroneos)
    } yield contrasena
  }

  /**
   * Validar los estados del usuario
   * @param usuario
   * @return
   */
  def validarEstado(usuario: UsuarioAgente): Future[Boolean] = {
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
  def validarContrasena(contrasena: String, usuario: UsuarioAgente, reintentosErroneos: Int): Future[Boolean] = {
    val hash = Crypto.hashSha512(contrasena.concat(AppendPasswordUser.appendUsuariosFiducia), usuario.id)
    if (hash.contentEquals(usuario.contrasena.get)) {
      Future.successful(true)
    } else {
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
  def validarCaducidadContrasena(tipoCliente: TiposCliente, usuario: UsuarioAgente, dias: Int): Future[Boolean] = {
    if (new DateTime().isAfter(new DateTime(usuario.fechaActualizacion.getTime).plusDays(dias))) {
      val token: String = Token.generarTokenCaducidadContrasena(tipoCliente, usuario.id)
      Future.failed(ValidacionException("401.9", token))
    } else {
      Future.successful(true)
    }
  }

}
