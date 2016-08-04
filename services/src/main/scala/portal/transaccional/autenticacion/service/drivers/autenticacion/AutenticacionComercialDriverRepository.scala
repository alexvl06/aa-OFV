package portal.transaccional.autenticacion.service.drivers.autenticacion

import java.util.Date

import co.com.alianza.infrastructure.dto.Cliente
import co.com.alianza.persistence.entities.Usuario
import co.com.alianza.util.token.Token

import scala.concurrent.{ ExecutionContext, Future }

case class AutenticacionComercialDriverRepository()(implicit val ex: ExecutionContext)
    extends AutenticacionComercialRepository {

  /**
   * Redirigir a la autenticacion correspondiente
   * dependiendo del tipo de usuario
   * @param tipoUsuario
   * @param usuario
   * @param contrasena
   * @return
   */
  def autenticar(usuario: String, tipoUsuario: Int, contrasena: String): Future[String] = {
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
  def autenticarFiduciaria(): Future[String] = {
    for {
      token <- Future.successful("ya me autentique !!!")
    } yield token
  }

  def autenticarAdministrador(): Future[String] = {
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
