package portal.transaccional.autenticacion.service.drivers.autenticacion

import java.util.Date

import co.com.alianza.commons.enumerations.TiposCliente
import co.com.alianza.constants.TiposConfiguracion
import co.com.alianza.exceptions.ValidacionException
import co.com.alianza.infrastructure.dto.Cliente
import co.com.alianza.persistence.dto.UsuarioLdapDTO
import co.com.alianza.persistence.entities.{ Usuario, UsuarioComercial }
import co.com.alianza.util.token.Token
import portal.transaccional.autenticacion.service.drivers.configuracion.ConfiguracionRepository
import portal.transaccional.autenticacion.service.drivers.ldap.LdapRepository
import portal.transaccional.autenticacion.service.drivers.usuarioComercial.UsuarioComercialRepository
import portal.transaccional.autenticacion.service.drivers.usuarioComercialAdmin.UsuarioComercialAdminRepository

import scala.concurrent.{ ExecutionContext, Future }

case class AutenticacionComercialDriverRepository(ldapRepo: LdapRepository, usuarioComercialRepo: UsuarioComercialRepository,
  usuarioComercialAdminRepo: UsuarioComercialAdminRepository, configuracionRepo: ConfiguracionRepository)
  (implicit val ex: ExecutionContext) extends AutenticacionComercialRepository {

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
      case `administrador` => autenticarAdministrador()
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
    //TODO: actualizar usuario cuando se loguéa bien
    //TODO: actualizar usuario cuando se loguéa mal
    for {
      cliente <- ldapRepo.autenticarLdap(usuario, tipoUsuario, password)
      usuario <- usuarioComercialRepo.getByUser(cliente.usuario)
      inactividad <- configuracionRepo.getConfiguracion(TiposConfiguracion.EXPIRACION_SESION.llave)
      token <- generarTokenComercial(cliente, tipoUsuario, ip, inactividad.valor)
      _ <- usuarioComercialRepo.actualizarToken(usuario.id, token)
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
  def autenticarAdministrador(): Future[String] = {
    //TODO: actualizar usuario cuando se loguéa bien
    //TODO: actualizar usuario cuando se loguéa mal
    for {
      usuario <- usuarioComercialRepo.getByUser(cliente.usuario)
      inactividad <- configuracionRepo.getConfiguracion(TiposConfiguracion.EXPIRACION_SESION.llave)
      token <- generarTokenComercial(cliente, tipoUsuario, ip, inactividad.valor)
      _ <- usuarioComercialRepo.actualizarToken(usuario.id, token)
    } yield token
    Future.failed(ValidacionException("401.2", "Error login admin comercial no implementado"))
  }

  /**
   * Generar token
   * @param cliente
   * @param ip
   * @param inactividad
   * @return
   */
  private def generarTokenComercial(cliente: UsuarioLdapDTO, tipoUsuario: Int, ip: String, inactividad: String): Future[String] = Future {
    val fiduciaria = TiposCliente.comercialFiduciaria.id
    val tipoCliente = {
      tipoUsuario match {
        case `fiduciaria` => TiposCliente.comercialFiduciaria
        case _ => TiposCliente.comercialValores
      }
    }
    Token.generarToken(cliente.nombre, cliente.identificacion.get, "", ip, new Date(System.currentTimeMillis()), inactividad, tipoCliente)
  }

  /**
   * Generar token
   * @param usuario Usuario que accede al servicio
   * @param cliente  Cliente
   * @param ip Ip por la cual accede el usuario
   * @param inactividad Inactividad del usuario
   * @return
   */
  private def generarTokenAdminComercial(usuario: Usuario, cliente: Cliente, ip: String, inactividad: String): Future[String] = Future {
    Token.generarToken(cliente.wcli_nombre, cliente.wcli_dir_correo, cliente.wcli_person,
      usuario.ipUltimoIngreso.getOrElse(ip), usuario.fechaUltimoIngreso.getOrElse(new Date(System.currentTimeMillis())), inactividad)
  }

}
