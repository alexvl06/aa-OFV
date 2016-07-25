package portal.transaccional.autenticacion.service.drivers.usuario

import co.com.alianza.exceptions.ValidacionException
import co.com.alianza.persistence.entities.Usuario
import co.com.alianza.util.clave.Crypto
import enumerations.{ AppendPasswordUser, EstadosUsuarioEnum, TipoIdentificacion }
import portal.transaccional.fiduciaria.autenticacion.storage.daos.daos.driver.UsuarioDAOs

import scala.concurrent.{ ExecutionContext, Future }

/**
 * Created by hernando on 25/07/16.
 */
case class UsuarioDriverRepository(usuarioDAO: UsuarioDAOs)(implicit val ex: ExecutionContext) extends UsuarioRepository {

  def getByIdentificacion(numeroIdentificacion: String): Future[Usuario] = {
    usuarioDAO.getByIdentity(numeroIdentificacion) flatMap {
      (usuarioOption: Option[Usuario]) =>
        usuarioOption match {
          case Some(usuario: Usuario) => Future.successful(usuario)
          case _ => Future.failed(ValidacionException("401.3", "Error usuario no existe"))
        }
    }
  }

  /**
   * Valida el estado del usuario
   * @param estadoUsuario El estado del usuario a validar
   * @return Future[Boolean]
   * Success => True
   * ErrorAutenticacion => ErrorUsuarioBloqueadoIntentosErroneos || ErrorUsuarioBloqueadoPendienteActivacion || ErrorUsuarioBloqueadoPendienteReinicio
   */
  def validarEstados(estadoUsuario: Int): Future[Boolean] = {
    if (estadoUsuario == EstadosUsuarioEnum.bloqueContraseña.id)
      Future.failed(ValidacionException("401.8", "Usuario Bloqueado"))
    else if (estadoUsuario == EstadosUsuarioEnum.pendienteActivacion.id)
      Future.failed(ValidacionException("401.10", "Usuario Bloqueado"))
    else if (estadoUsuario == EstadosUsuarioEnum.pendienteReinicio.id)
      Future.failed(ValidacionException("401.12", "Usuario Bloqueado"))
    else Future.successful(true)
  }

  /**
   * Valida que las contrasenas concuerden
   * @param contrasena Password de la peticion
   * @param contrasenaUsuario Password del usuario en BD
   * @return  Future[Boolean]
   */
  def validarContrasena(contrasena: String, contrasenaUsuario: String, idUsuario: Int): Future[Boolean] = {
    val hash = Crypto.hashSha512(contrasena.concat(AppendPasswordUser.appendUsuariosFiducia), idUsuario)
    if (hash.contentEquals(contrasenaUsuario)) Future.successful(true)
    else Future.failed(ValidacionException("401.3", "Error Credenciales"))
  }

  /**
   * Validar que correspondan los tipos de identificación
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
   * Asociar el token al usuario
   * @param numeroIdentificacion
   * @param token
   * @return
   */
  def actualizarToken(numeroIdentificacion: String, token: String): Future[Int] = {
    usuarioDAO.createToken(numeroIdentificacion, token)
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
   * Devuelve la naturaleza de la persona
   * @param idTipoIdent Id del tipo de identificacion
   * @return F si es fiduciaria, J si es juridica y N si es natural
   */
  private def getNaturaleza(idTipoIdent: Int): String = {
    idTipoIdent match {
      case TipoIdentificacion.FID.identificador => "F"
      case TipoIdentificacion.NIT.identificador => "J"
      case TipoIdentificacion.SOCIEDAD_EXTRANJERA.identificador => "S"
      case _ => "N"
    }
  }

}
