package portal.transaccional.autenticacion.service.drivers.autenticacion

import java.util.Date

import co.com.alianza.infrastructure.dto.Cliente
import co.com.alianza.persistence.entities.Usuario
import co.com.alianza.util.token.Token
import portal.transaccional.autenticacion.service.drivers.ldap.LdapRepository

import scala.concurrent.{ ExecutionContext, Future }

case class AutenticacionComercialDriverRepository(ldapRepo: LdapRepository)(implicit val ex: ExecutionContext)
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
    autenticarFiduciaria(usuario, tipoUsuario, contrasena)
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
  def autenticarFiduciaria(usuario: String, tipoUsuario: Int, password: String): Future[String] = {
    for {
      cliente <- ldapRepo.autenticarLdap(usuario, tipoUsuario, password)
      token <- Future.successful("eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9.eyJ0aXBvQ2xpZW50ZSI6ImFnZW50ZUVtcHJlc2FyaWFsIiwibmJmIjoxNDQwNzg5NDMzLCJ0aXBvSWRlbnRpZmljYWNpb24iOiJKIiwidWx0aW1hRmVjaGFJbmdyZXNvIjoiMTAgYWdvc3RvLCAyMDE1IGEgbGFzIDA0OjA5IFBNIiwidWx0aW1hSXBJbmdyZXNvIjoiMTI3LjAuMC4xIiwiZXhwaXJhY2lvbkluYWN0aXZpZGFkIjoiOTk5IiwiY29ycmVvIjoiZmRAc2FkLmNvIiwibml0IjoiODkwMTE0Nzc4IiwiaXNzIjoiaHR0cDpcL1wvZmlkdWNpYXJpYS5hbGlhbnphLmNvbS5jbyIsIm5vbWJyZVVzdWFyaW8iOiJhcHJvYmFkb3IiLCJleHAiOjE0NDA3OTEyMzMsImlhdCI6MTQ0MDc4OTQzM30.LKlmlZynUL95PS9Z_DAg05-KMgDCdWczLR3bfiPbOMvbV0HQA6PR10buywhUgX5glddujHnQslQ_8_sHi1jq2w")
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
