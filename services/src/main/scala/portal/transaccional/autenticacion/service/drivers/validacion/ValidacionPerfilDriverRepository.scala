package portal.transaccional.autenticacion.service.drivers.validacion

import akka.util.Timeout
import co.com.alianza.commons.enumerations.TiposCliente
import co.com.alianza.constants.LlavesReglaContrasena
import co.com.alianza.exceptions.ValidacionException
import co.com.alianza.persistence.entities.{ Usuario, ValidacionPerfil }
import co.com.alianza.util.token.AesUtil
import portal.transaccional.autenticacion.service.drivers.cliente.ClienteRepository
import portal.transaccional.autenticacion.service.drivers.ipusuario.IpUsuarioRepository
import portal.transaccional.autenticacion.service.drivers.reglas.ReglaContrasenaRepository
import portal.transaccional.autenticacion.service.drivers.respuesta.RespuestaUsuarioRepository
import portal.transaccional.autenticacion.service.drivers.usuarioIndividual.UsuarioRepository
import portal.transaccional.fiduciaria.autenticacion.storage.daos.portal.{ AlianzaDAO, AlianzaDAOs }

import scala.concurrent.duration._
import scala.concurrent.{ ExecutionContext, Future }

/**
 *
 * @param alianzaDAO
 * @param ipRepo
 * @param respuestasRepo
 * @param reglaRepo
 * @param usuarioRepo
 * @param clienteRepository
 */
case class ValidacionPerfilDriverRepository(
    alianzaDAO: AlianzaDAOs,
    ipRepo: IpUsuarioRepository,
    respuestasRepo: RespuestaUsuarioRepository,
    reglaRepo: ReglaContrasenaRepository,
    usuarioRepo: UsuarioRepository,
    clienteRepository: ClienteRepository
)(implicit val ex: ExecutionContext) extends ValidacionPerfilRepository {

  implicit val timeout = Timeout(5.seconds)

  /**
   * Obtiene los tipos de validación relacionadas a un perfil
   * @param idPerfil Identificador del perfil
   * @return Future[Seq[ValidacionPerfil]]
   */
  def getValidacion(idPerfil: Int): Future[Seq[ValidacionPerfil]] = {
    alianzaDAO.getValidacionByPerfil(idPerfil) flatMap {
      (perfilValida: Seq[ValidacionPerfil]) =>
        perfilValida match {
          case validaPer: Seq[ValidacionPerfil] => Future.successful(validaPer)
          case _ => Future.failed(ValidacionException("404.1", "Error perfil sin validaciones"))
        }
    }
  }

  /**
   * Realiza la validación de ip de confianza.
   * @pre   Para poder utilizar esta validación el usuario debe estar logeado y debe existir en base de datos.
   * @param idUsuario Identificador unico del usuario
   * @param token Token de sesion
   * @param ip Ip origen peticion
   * @return Future[String]
   */
  def validarIpConfianza(idUsuario: Int, token: String, ip: String): Future[String] = {
    for {
      respuestas <- respuestasRepo.getRespuestasById(idUsuario)
      ips <- ipRepo.getIpsUsuarioById(idUsuario)
      validacionIps <- ipRepo.validarControlIp(ip, ips, token, respuestas.nonEmpty)
    } yield validacionIps
  }

  /**
   * Genera token sin validación de ip.
   * @pre   Para poder utilizar esta validación el usuario debe estar logeado y debe existir en base de datos.
   * @param token Token de sesion
   * @return Future[String]
   */
  def noValidaIp(token: String): Future[String] = {
    val encryptedToken: String = AesUtil.encriptarToken(token)
    Future.successful(encryptedToken)
  }

  /**
   * Realiza la validación de contraseña teniendo en cuenta las cantidad de intentos fallidos
   * @param contrasena
   * @param usuario
   * @return Future[Boolean]
   */
  def validarPassConReintento(contrasena: String, usuario: Usuario): Future[Boolean] = {
    for {
      reintentos <- reglaRepo.getRegla(LlavesReglaContrasena.CANTIDAD_REINTENTOS_INGRESO_CONTRASENA)
      contrasena <- usuarioRepo.validarContrasena(contrasena, usuario, reintentos.valor.toInt)
    } yield contrasena
  }

  /**
   * Realiza la validación de contraseña
   * @param contrasena
   * @param usuario
   * @return Future[Boolean]
   */
  def validarPassSinReintento(contrasena: String, usuario: Usuario): Future[Boolean] = {
    for {
      contrasena <- usuarioRepo.validarContrasena(contrasena, usuario)
    } yield contrasena
  }

  /**
   * Valida el estado de usuario en sifi.
   * @param numeroIdentificacion
   * @param tipoIdentificacion
   * @return
   */
  def validarEstadoSifi(numeroIdentificacion: String, tipoIdentificacion: Option[Int]): Future[Boolean] = {
    for {
      cliente <- clienteRepository.getCliente(numeroIdentificacion, tipoIdentificacion)
      estado <- clienteRepository.validarEstado(cliente)
    } yield estado
  }

  /**
   * Valida si la clave ya caduco.
   * @param usuario
   * @return
   */
  def validarCaducidad(usuario: Usuario): Future[Boolean] = {
    for {
      reglaDias <- reglaRepo.getRegla(LlavesReglaContrasena.DIAS_VALIDA)
      valida <- usuarioRepo.validarCaducidadContrasena(TiposCliente.clienteIndividual, usuario, reglaDias.valor.toInt)
    } yield valida
  }

}
