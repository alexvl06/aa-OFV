package portal.transaccional.autenticacion.service.drivers.usuarioIndividual

import java.sql.Timestamp

import co.com.alianza.commons.enumerations.TiposCliente._
import co.com.alianza.exceptions.ValidacionException
import co.com.alianza.persistence.entities.Usuario
import co.com.alianza.util.clave.Crypto
import co.com.alianza.util.token.Token
import enumerations.{ AppendPasswordUser, EstadosUsuarioEnum }
import org.joda.time.DateTime
import portal.transaccional.fiduciaria.autenticacion.storage.daos.portal.UsuarioDAOs

import scala.concurrent.{ ExecutionContext, Future }

/**
 * Created by hernando on 25/07/16.
 */
case class UsuarioDriverRepository(usuarioDAO: UsuarioDAOs)(implicit val ex: ExecutionContext) extends UsuarioRepository {

  def getByIdentificacion(numeroIdentificacion: String): Future[Usuario] = {
    println("VALIDACION 1")
    usuarioDAO.getByIdentity(numeroIdentificacion) flatMap {
      (usuarioOption: Option[Usuario]) =>
        usuarioOption match {
          case Some(usuario: Usuario) => Future.successful(usuario)
          case ex: Any => println(ex); Future.failed(ValidacionException("401.3", "Error usuario no existe"))
        }
    }
  }

  def getByToken(token: String): Future[Option[Usuario]] = usuarioDAO.getByToken(token)

  /**
   * Asociar el token al usuario
   *
   * @param numeroIdentificacion
   * @param token
   * @return
   */
  def actualizarToken(numeroIdentificacion: String, token: String): Future[Int] = {
    usuarioDAO.createToken(numeroIdentificacion, token)
  }

  /**
   * Actualiza los ingresos erroneos de un usuario al numero especificado por parametro
   *
   * @param idUsuario
   * @param numeroIntentos
   * @return
   */
  def actualizarIngresosErroneosUsuario(idUsuario: Int, numeroIntentos: Int): Future[Int] = {
    usuarioDAO.updateIncorrectEntries(idUsuario, numeroIntentos)
  }

  /**
   * Actualizar ultima ip
   *
   * @param numeroIdentificacion
   * @param ip
   * @return
   */
  def actualizarIp(numeroIdentificacion: String, ip: String): Future[Int] = {
    usuarioDAO.updateLastIp(numeroIdentificacion, ip)
  }

  /**
   * Actualizar fecha ingreso
   *
   * @param numeroIdentificacion
   * @param fechaActual
   * @return
   */
  def actualizarFechaIngreso(numeroIdentificacion: String, fechaActual: Timestamp): Future[Int] = {
    usuarioDAO.updateLastDate(numeroIdentificacion, fechaActual)
  }

  /////////////////////////////// validaciones //////////////////////////////////

  /**
   * Valida el estado del usuario
   *
   * @param estado El estado del usuario a validar
   * @return Future[Boolean]
   */
  def validarEstado(estado: Int): Future[Boolean] = {
    if (estado == EstadosUsuarioEnum.bloqueContraseña.id)
      Future.failed(ValidacionException("401.8", "Usuario Bloqueado"))
    else if (estado == EstadosUsuarioEnum.pendienteActivacion.id)
      Future.failed(ValidacionException("401.10", "Usuario Bloqueado"))
    else if (estado == EstadosUsuarioEnum.pendienteReinicio.id)
      Future.failed(ValidacionException("401.12", "Usuario Bloqueado"))
    else Future.successful(true)
  }

  /**
   * Valida que las contrasenas concuerden
   *
   * @param contrasena
   * @param usuario
   * @param reintentosErroneos
   * @return
   */
  def validarContrasena(contrasena: String, usuario: Usuario, reintentosErroneos: Int): Future[Boolean] = {
    val hash = Crypto.hashSha512(contrasena.concat(AppendPasswordUser.appendUsuariosFiducia), usuario.id.get)
    if (hash.contentEquals(usuario.contrasena.get)) {
      Future.successful(true)
    } else {
      //si las contraseñas no concuerdan:
      //1. actualizar ingresos erroneos
      //2. bloquear usuario si es necesario
      val reintentos: Int = usuario.numeroIngresosErroneos + 1
      for {
        actualizarIngresos <- actualizarIngresosErroneosUsuario(usuario.id.get, reintentos)
        bloquear <- validarBloqueoUsuario(usuario.id.get, reintentos, reintentosErroneos)
        error <- Future.failed(ValidacionException("401.3", "Error Credenciales"))
      } yield error
    }
  }

  /**
   * Bloquea el usuario si se incumple la regla por parametro
   *
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
   * Validar que correspondan los tipos de identificación
   *
   * @param tipoIdentificacion
   * @param tipoIdentificacionUsuario
   * @return
   */
  def validarTipoIdentificacion(tipoIdentificacion: Int, tipoIdentificacionUsuario: Int): Future[Boolean] = {
    val validacion = tipoIdentificacion == tipoIdentificacionUsuario
    validacion match {
      case false => Future.failed(ValidacionException("401.3", "Error Credenciales"))
      case true => Future.successful(true)
    }
  }

  /**
   * Valida la fecha de caducidad de la contraseña de un usuario
   *
   * @param tipoCliente
   * @param usuario
   * @param dias
   * @return
   */
  def validarCaducidadContrasena(tipoCliente: TiposCliente, usuario: Usuario, dias: Int): Future[Boolean] = {
    if (new DateTime().isAfter(new DateTime(usuario.fechaActualizacion.getTime).plusDays(dias))) {
      val token: String = Token.generarTokenCaducidadContrasena(tipoCliente, usuario.id.get)
      Future.failed(ValidacionException("401.9", token))
    } else {
      Future.successful(true)
    }
  }

  /**
   * Invalidar el token al usuario
   *
   * @param token
   * @return
   */
  def invalidarToken(token: String): Future[Int] = {
    usuarioDAO.deleteToken(token) flatMap {
      case r: Int => Future.successful(r)
      case _ => Future.failed(ValidacionException("401.9", "No se pudo borrar el token"))
    }
  }

}
