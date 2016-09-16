package portal.transaccional.autenticacion.service.drivers.autenticacion

import java.sql.Timestamp
import java.util.Date

import co.com.alianza.commons.enumerations.TiposCliente
import co.com.alianza.commons.enumerations.TiposCliente
import co.com.alianza.commons.enumerations.TiposCliente.TiposCliente
import co.com.alianza.constants.TiposConfiguracion
import co.com.alianza.exceptions.ValidacionException
import co.com.alianza.infrastructure.dto.Cliente
import co.com.alianza.persistence.dto.UsuarioLdapDTO
import co.com.alianza.persistence.entities.{ Usuario, UsuarioComercial, UsuarioComercialAdmin }
import co.com.alianza.util.token.{ AesUtil, Token }
import portal.transaccional.autenticacion.service.drivers.configuracion.ConfiguracionRepository
import portal.transaccional.autenticacion.service.drivers.ldap.LdapRepository
import portal.transaccional.autenticacion.service.drivers.sesion.SesionRepository
import portal.transaccional.autenticacion.service.drivers.usuarioComercial.UsuarioComercialRepository
import portal.transaccional.autenticacion.service.drivers.usuarioComercialAdmin.UsuarioComercialAdminRepository

import scala.concurrent.{ ExecutionContext, Future }

case class AutenticacionComercialDriverRepository(
  ldapRepo: LdapRepository,
  usuarioComercialRepo: UsuarioComercialRepository, usuarioComercialAdminRepo: UsuarioComercialAdminRepository,
  configuracionRepo: ConfiguracionRepository, sesionRepo: SesionRepository
)(implicit val ex: ExecutionContext)
    extends AutenticacionComercialRepository {

  val MAX_INACTIVIDAD: Int = 30000

  /**
   * Redirigir a la autenticacion correspondiente
   * dependiendo del tipo de usuario
   * @param tipoUsuario
   * @param usuario
   * @param contrasena
   * @return
   */
  def autenticar(usuario: String, tipoUsuario: Int, contrasena: String, ip: String): Future[String] = {
    val administrador = TiposCliente.comercialAdmin.id
    val valores = TiposCliente.comercialValores.id
    val fiduciaria = TiposCliente.comercialFiduciaria.id
    val servicioCliente = TiposCliente.comercialSAC.id
    tipoUsuario match {
      case `administrador` => autenticarAdministrador(usuario, contrasena, ip)
      case `valores` => autenticarComercial(usuario, TiposCliente.comercialValores, contrasena, ip)
      case `fiduciaria` => autenticarComercial(usuario, TiposCliente.comercialFiduciaria, contrasena, ip)
      case `servicioCliente` => autenticarNoComercial(usuario, TiposCliente.comercialSAC, contrasena, ip)
      case _ => Future.failed(new ValidacionException("401.1", "Credenciales inválidas"))
    }
  }

  /**
   * Flujo:
   * - obtener cliente ldap
   * - obtener usuario
   * - obtener llave inactividad
   * - generar token
   * - asociar token
   * - crear session de usuario
   */
  def autenticarComercial(usuario: String, tipoCliente: TiposCliente, password: String, ip: String): Future[String] = {
    for {
      cliente <- ldapRepo.autenticarLdap(usuario, tipoCliente, password)
      _ <- ldapRepo.validarSACLdap(cliente, false)
      existe <- usuarioComercialRepo.existeUsuario(usuario)
      _ <- if (!existe) usuarioComercialRepo.crearUsuario(usuario, ip) else Future(true)
      usuarioComercial <- usuarioComercialRepo.getUser(cliente.usuario)
      _ <- usuarioComercialRepo.updateIpFecha(usuario, ip)
      inactividad <- configuracionRepo.getConfiguracion(TiposConfiguracion.EXPIRACION_SESION.llave)
      token <- generarTokenComercial(cliente, "C", usuarioComercial, tipoCliente, ip, inactividad.valor)
      _ <- usuarioComercialRepo.crearToken(usuarioComercial.id, AesUtil.encriptarToken(token))
      sesion <- sesionRepo.crearSesion(token, inactividad.valor.toInt, None)
    } yield token
  }

  /**
   * Flujo:
   * - obtener cliente ldap
   * - obtener usuario
   * - obtener llave inactividad
   * - generar token
   * - asociar token
   * - crear session de usuario
   */
  def autenticarNoComercial(usuario: String, tipoCliente: TiposCliente, password: String, ip: String): Future[String] = {
    for {
      cliente <- ldapRepo.autenticarLdap(usuario, tipoCliente, password)
      _ <- ldapRepo.validarSACLdap(cliente, true)
      existe <- usuarioComercialRepo.existeUsuario(usuario)
      _ <- if (!existe) usuarioComercialRepo.crearUsuario(usuario, ip) else Future(true)
      usuarioComercial <- usuarioComercialRepo.getUser(cliente.usuario)
      _ <- usuarioComercialRepo.updateIpFecha(usuario, ip)
      inactividad <- Future(MAX_INACTIVIDAD) //maximo numero que soporta el front end
      token <- generarTokenComercial(cliente, "SAC", usuarioComercial, tipoCliente, ip, inactividad.toString)
      _ <- usuarioComercialRepo.crearToken(usuarioComercial.id, AesUtil.encriptarToken(token))
    } yield token
  }

  /**
   * Flujo:
   * - obtener usuario
   * - validar contrasena
   * - obtener llave inactividad
   * - generar token
   * - asociar token
   * - crear session de usuario
   */
  def autenticarAdministrador(usuario: String, contrasena: String, ip: String): Future[String] = {
    for {
      usuario <- usuarioComercialAdminRepo.obtenerUsuario(usuario)
      _ <- usuarioComercialAdminRepo.validarContrasena(contrasena, usuario)
      inactividad <- Future(MAX_INACTIVIDAD) //maximo numero que soporta el front end
      token <- generarTokenAdminComercial(usuario, ip, inactividad.toString)
      _ <- usuarioComercialAdminRepo.crearToken(usuario.id, AesUtil.encriptarToken(token))
      actualizarIP <- usuarioComercialAdminRepo.actualizarIp(usuario.id, ip)
      actualizarFechaUltimoIngreso <- usuarioComercialAdminRepo.actualizarFechaIngreso(usuario.id, new Timestamp((new Date).getTime))
    } yield token
  }

  /**
   * Generar token
   * @param cliente
   * @param ip
   * @param inactividad
   * @return
   */
  private def generarTokenComercial(cliente: UsuarioLdapDTO, tipoIdentificacion: String, usuario: UsuarioComercial,
    tipoCliente: TiposCliente, ip: String, inactividad: String): Future[String] = {
    val token = Token.generarToken(usuario.usuario, cliente.identificacion.get, tipoIdentificacion, usuario.ipUltimoIngreso.getOrElse(ip),
      usuario.fechaUltimoIngreso.getOrElse(new Date(System.currentTimeMillis())), inactividad, tipoCliente)
    Future.successful(token)
  }

  /**
   * Generar token
   * @param usuario Usuario que accede al servicio
   * @param ip Ip por la cual accede el usuario
   * @param inactividad Inactividad del usuario
   * @return
   */
  private def generarTokenAdminComercial(usuario: UsuarioComercialAdmin, ip: String, inactividad: String): Future[String] = Future {
    Token.generarToken(usuario.usuario, usuario.correo, "CA", usuario.ipUltimoIngreso.getOrElse(ip), usuario.fechaUltimoIngreso.getOrElse(
      new Date(System.currentTimeMillis())
    ), inactividad, TiposCliente.comercialAdmin)
  }

}
