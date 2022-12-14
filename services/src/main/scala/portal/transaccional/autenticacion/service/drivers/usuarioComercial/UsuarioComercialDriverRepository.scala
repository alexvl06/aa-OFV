package portal.transaccional.autenticacion.service.drivers.usuarioComercial

/**
 * Created by alexandra on 5/08/16.
 */

import java.sql.Timestamp

import co.com.alianza.exceptions.ValidacionException
import co.com.alianza.persistence.entities.UsuarioComercial
import co.com.alianza.util.clave.Crypto
import enumerations.{ AppendPasswordUser, EstadosUsuarioEnum }
import portal.transaccional.fiduciaria.autenticacion.storage.daos.portal.UsuarioComercialDAOs

import scala.concurrent.{ ExecutionContext, Future }

/**
 * Created by hernando on 2016
 */
case class UsuarioComercialDriverRepository(usuarioDAO: UsuarioComercialDAOs)(implicit val ex: ExecutionContext) extends UsuarioComercialRepository {

  def getUser(usuario: String): Future[UsuarioComercial] = {
    getByUser(usuario) flatMap {
      (usuarioOption: Option[UsuarioComercial]) =>
        usuarioOption match {
          case Some(usuario: UsuarioComercial) => Future.successful(usuario)
          case ex: Any => Future.failed(ValidacionException("401.3", "Error usuario no existe"))
        }
    }
  }

  def getByUser(usuario: String): Future[Option[UsuarioComercial]] = {
    usuarioDAO.getByUser(usuario)
  }

  def updateIpFecha(nombreUsuario: String, ip: String): Future[Int] = usuarioDAO.updateIpFecha(nombreUsuario, ip)

  //def getByUser(name : String) : Future [UsuarioComercial] = usuarioDAO.getById()

  def getByToken(token: String): Future[Option[UsuarioComercial]] = usuarioDAO.getByToken(token)

  /**
   * Asociar el token al usuarioComercial
   *
   * @param idUsuario
   * @param token
   * @return
   */
  def crearToken(idUsuario: Int, token: String): Future[Int] = usuarioDAO.createToken(idUsuario, token)

  /**
   * Eiminar el token al usuarioComercial
   *
   * @param token
   * @return
   */
  def eliminarToken(token: String): Future[Int] = usuarioDAO.deleteToken(token)

  /////////////////////////////// validaciones //////////////////////////////////

  /**
   * Valida el estado del usuario
   *
   * @param estado El estado del usuario a validar
   * @return Future[Boolean]
   */
  def validarEstado(estado: Int): Future[Boolean] = {
    if (estado == EstadosUsuarioEnum.bloqueContrase??a.id) {
      Future.failed(ValidacionException("401.8", "Usuario Bloqueado"))
    } else if (estado == EstadosUsuarioEnum.pendienteActivacion.id) {
      Future.failed(ValidacionException("401.10", "Usuario Bloqueado"))
    } else if (estado == EstadosUsuarioEnum.pendienteReinicio.id) {
      Future.failed(ValidacionException("401.12", "Usuario Bloqueado"))
    } else {
      Future.successful(true)
    }
  }

  /**
   * Valida que las contrasenas concuerden
   *
   * @param contrasenaIngresada Es la contrase??a que el usario ingreso en el front para autenticarse
   * @param usuario Es el usuarioComercial que desea autenticarse
   * @param contrasenaValida Es la contrase??a del usuario traida desde el LDAP , para verificar la coincidencia con la contrase??a Ingresada
   * @return
   */
  def validarContrasena(contrasenaIngresada: String, usuario: UsuarioComercial, contrasenaValida: String): Future[Boolean] = {
    val hash = Crypto.hashSha512(contrasenaIngresada.concat(AppendPasswordUser.appendUsuariosFiducia), usuario.id)
    if (hash.contentEquals(contrasenaValida)) {
      Future.successful(true)
    } else {
      Future.failed(ValidacionException("401.3", "Error Credenciales"))
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

  def existeUsuario(nombreUsuario: String): Future[Boolean] = usuarioDAO.existeUsuario(nombreUsuario)

  def crearUsuario(nombreUsuario: String, ip: String): Future[Int] = usuarioDAO.create(nombreUsuario, ip)
}
