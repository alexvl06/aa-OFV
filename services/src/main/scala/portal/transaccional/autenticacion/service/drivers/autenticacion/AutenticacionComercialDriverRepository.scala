package portal.transaccional.autenticacion.service.drivers.autenticacion

import java.sql.Timestamp
import java.util.Date

import co.com.alianza.commons.enumerations.TiposCliente
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

case class AutenticacionComercialDriverRepository(ldapRepo: LdapRepository, usuarioComercialRepo: UsuarioComercialRepository,
    usuarioComercialAdminRepo: UsuarioComercialAdminRepository, configuracionRepo: ConfiguracionRepository, sesionRepo: SesionRepository)(implicit val ex: ExecutionContext) extends AutenticacionComercialRepository {

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
    tipoUsuario match {
      case `administrador` => autenticarAdministrador(usuario, contrasena, ip)
      case _ => autenticarComercial(usuario, tipoUsuario, contrasena, ip)
    }
  }

  /**
   * Flujo:
   * - obtener cliente ldap
   * - obtener usuario
   * - TODO: validar caducidad
   * - TODO: validar ingresos erróneos
   * - obtener llave inactividad
   * - generar token
   * - asociar token
   * - crear session de usuario
   */
  def autenticarComercial(usuario: String, tipoUsuario: Int, password: String, ip: String): Future[String] = {
    for {
      cliente <- ldapRepo.autenticarLdap(usuario, tipoUsuario, password)
      usuarioComercial <- usuarioComercialRepo.getByUser(cliente.usuario)
      _ <- usuarioComercialRepo.update(usuario, ip)
      inactividad <- configuracionRepo.getConfiguracion(TiposConfiguracion.EXPIRACION_SESION.llave)
      token <- generarTokenComercial(cliente, usuarioComercial, tipoUsuario, ip, inactividad.valor)
      _ <- usuarioComercialRepo.crearToken(usuarioComercial.id, AesUtil.encriptarToken(token))
      sesion <- sesionRepo.crearSesion(token, inactividad.valor.toInt, None)
    } yield token
  }

  /**
   * Flujo:
   * - obtener usuario
   * - validar contrasena
   * - TODO: validar caducidad
   * - TODO: validar ingresos erróneos
   * - obtener llave inactividad
   * - generar token
   * - asociar token
   * - crear session de usuario
   */
  def autenticarAdministrador(usuario: String, contrasena: String, ip: String): Future[String] = {
    for {
      usuario <- usuarioComercialAdminRepo.obtenerUsuario(usuario)
      _ <- usuarioComercialAdminRepo.validarContrasena(contrasena, usuario)
      inactividad <- configuracionRepo.getConfiguracion(TiposConfiguracion.EXPIRACION_SESION.llave)
      token <- generarTokenAdminComercial(usuario, ip, inactividad.valor)
      _ <- usuarioComercialAdminRepo.crearToken(usuario.id, AesUtil.encriptarToken(token))
      sesion <- sesionRepo.crearSesion(token, inactividad.valor.toInt, None)
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
  private def generarTokenComercial(cliente: UsuarioLdapDTO, usuario: UsuarioComercial, tipoUsuario: Int, ip: String, inactividad: String): Future[String] =
    Future {
      val fiduciaria = TiposCliente.comercialFiduciaria.id
      val tipoCliente = {
        tipoUsuario match {
          case `fiduciaria` => TiposCliente.comercialFiduciaria
          case _ => TiposCliente.comercialValores
        }
      }
      Token.generarToken(usuario.usuario, cliente.identificacion.get, "", usuario.ipUltimoIngreso.getOrElse(ip), usuario.fechaUltimoIngreso.getOrElse(
        new Date(System.currentTimeMillis())
      ), inactividad, tipoCliente)
    }

  /**
   * Generar token
   * @param usuario Usuario que accede al servicio
   * @param ip Ip por la cual accede el usuario
   * @param inactividad Inactividad del usuario
   * @return
   */
  private def generarTokenAdminComercial(usuario: UsuarioComercialAdmin, ip: String, inactividad: String): Future[String] = Future {
    Token.generarToken(usuario.usuario, usuario.correo, "", usuario.ipUltimoIngreso.getOrElse(ip), usuario.fechaUltimoIngreso.getOrElse(
      new Date(System.currentTimeMillis())
    ), inactividad, TiposCliente.comercialAdmin)
  }

}
