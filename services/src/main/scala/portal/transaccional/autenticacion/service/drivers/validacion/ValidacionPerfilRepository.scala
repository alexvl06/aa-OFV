package portal.transaccional.autenticacion.service.drivers.validacion

import co.com.alianza.persistence.entities.{ Usuario, ValidacionPerfil }

import scala.concurrent.Future

trait ValidacionPerfilRepository {
  /**
   * Obtiene los tipos de validación relacionadas a un perfil
   * @param idPerfil Identificador del perfil
   * @return SeqValidacionPerfil
   */
  def getValidacion(idPerfil: Int): Future[Seq[ValidacionPerfil]]

  /**
   * Realiza la validación de ip de confianza.
   * pre   Para poder utilizar esta validación el usuario debe estar logeado y debe existir en base de datos.
   * @param idUsuario Identificador unico del usuario
   * @param token Token de sesion
   * @param ip Ip origen peticion
   * @return Future[String]
   */
  def validarIpConfianza(idUsuario: Int, token: String, ip: String): Future[String]

  /**
   * Realiza la validación de contraseña teniendo en cuenta las cantidad de intentos fallidos
   * @param contrasena
   * @param usuario
   * @return Future[Boolean]
   */
  def validarPassConReintento(contrasena: String, usuario: Usuario): Future[Boolean]

  /**
   * Realiza la validación de contraseña
   * @param contrasena
   * @param usuario
   * @return Future[Boolean]
   */
  def validarPassSinReintento(contrasena: String, usuario: Usuario): Future[Boolean]

  /**
   * Valida el estado de usuario en sifi.
   * @param numeroIdentificacion
   * @param tipoIdentificacion
   * @return  Future[Boolean]
   */
  def validarEstadoSifi(numeroIdentificacion: String, tipoIdentificacion: Option[Int]): Future[Boolean]

  /**
   * Valida si la clave ya caduco.
   * @param usuario
   * @return  Future[Boolean]
   */
  def validarCaducidad(usuario: Usuario): Future[Boolean]

  /**
   * Genera token sin validación de ip.
   * pre   Para poder utilizar esta validación el usuario debe estar logeado y debe existir en base de datos.
   * @param token Token de sesion
   * @return Future[String]
   */
  def noValidaIp(token: String): Future[String]

}
