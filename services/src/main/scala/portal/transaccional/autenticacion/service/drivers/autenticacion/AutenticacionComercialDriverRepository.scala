package portal.transaccional.autenticacion.service.drivers.autenticacion

import java.util.Date

import co.com.alianza.commons.enumerations.TiposCliente
import co.com.alianza.infrastructure.dto.Cliente
import co.com.alianza.persistence.dto.UsuarioLdapDTO
import co.com.alianza.persistence.entities.{ Usuario, UsuarioComercial }
import co.com.alianza.util.token.Token
import portal.transaccional.autenticacion.service.drivers.ldap.LdapRepository
import portal.transaccional.autenticacion.service.drivers.usuarioComercial.UsuarioComercialRepository

import scala.concurrent.{ ExecutionContext, Future }

case class AutenticacionComercialDriverRepository(ldapRepo: LdapRepository, usuarioComercialRepo: UsuarioComercialRepository)(implicit val ex: ExecutionContext) {

  /**
   * Redirigir a la autenticacion correspondiente
   * dependiendo del tipo de usuario
   * @param tipoUsuario
   * @param usuario
   * @param contrasena
   * @return
   */
  def autenticar(usuario: String, tipoUsuario: Int, contrasena: String): Future[String] = {
    //TODO: agregar el match por tipo de usuario
    autenticarFiduciaria(usuario, tipoUsuario, contrasena)
  }

  def autenticarValores(usuario: String, tipoUsuario: Int, password: String): Future[String] = {
    for {
      cliente <- ldapRepo.autenticarLdap(usuario, tipoUsuario, password)
      usuario <- usuarioComercialRepo.getByUser(cliente.usuario)
      token <- generarTokenComercial(cliente, "ip", "100")
    } yield token

  }

  /**
   * Flujo:
   * - obtener usuario
   * - validar contrasena
   * - obtener cliente ldap
   * - validar caducidad
   * - generar token
   * - asociar token
   * - crear session de usuario
   */
  def autenticarFiduciaria(usuario: String, tipoUsuario: Int, password: String): Future[String] = {
    for {
      cliente <- ldapRepo.autenticarLdap(usuario, tipoUsuario, password)
      usuario <- usuarioComercialRepo.getByUser(cliente.usuario)
      token <- generarTokenComercial(cliente, "ip", "100")
    } yield token
  }

  def autenticarAdministrador(): Future[String] = {
    //TODO: Agregar validaciones
    Future.successful("")
  }

  /**
   * Generar token
   * @param cliente
   * @param ip
   * @param inactividad
   * @return
   */
  private def generarTokenComercial(cliente: UsuarioLdapDTO, ip: String, inactividad: String): Future[String] = Future {
    Token.generarToken(cliente.nombre, cliente.identificacion.get, "", ip, new Date(System.currentTimeMillis()), inactividad, TiposCliente.comercialFiduciaria)
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
