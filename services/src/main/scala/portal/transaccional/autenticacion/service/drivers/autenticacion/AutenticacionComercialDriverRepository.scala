package portal.transaccional.autenticacion.service.drivers.autenticacion

import java.util.Date

import co.com.alianza.infrastructure.dto.Cliente
import co.com.alianza.persistence.entities.Usuario
import co.com.alianza.util.token.Token

import scala.concurrent.Future

case class AutenticacionComercialDriverRepository ()
  extends AutenticacionComercialRepository {

  def autenticar(tipoUsuario: Int, usuario: String, contrasena: String, ip: String ): Future[String] = {
    autenticarFiduciaria()
  }

  def autenticarValores(): Future[String] = {
    Future.successful("")
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
  def autenticarFiduciaria() : Future[String] = {
    for{
      token <- Future.successful("")
    }yield token
  }

  def autenticarAdministrador() : Future[String] = {
    Future.successful("")
  }

  /**
   * Generar token
   * @param usuario Usuario que accede al servicio
   * @param cliente  Cliente
   * @param ip Ip por la cual accede el usuario
   * @param inactividad Inactividad del usuario
   * @return
   */
  private def generarTokenComercial(usuario: Usuario, cliente: Cliente, ip: String, inactividad: String): Future[String] = Future {
    Token.generarToken(cliente.wcli_nombre, cliente.wcli_dir_correo, cliente.wcli_person,
      usuario.ipUltimoIngreso.getOrElse(ip), usuario.fechaUltimoIngreso.getOrElse(new Date(System.currentTimeMillis())), inactividad)
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
