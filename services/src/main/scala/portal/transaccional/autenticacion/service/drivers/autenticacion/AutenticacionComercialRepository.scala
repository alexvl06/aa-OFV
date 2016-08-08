package portal.transaccional.autenticacion.service.drivers.autenticacion

import scala.concurrent.Future

trait AutenticacionComercialRepository {

  def autenticar(usuario: String, tipoUsuario: Int, contrasena: String, ip: String): Future[String]

  def autenticarComercial(usuario: String, tipoUsuario: Int, contrasena: String, ip: String): Future[String]

  def autenticarAdministrador(usuario: String, contrasena: String, ip: String): Future[String]

}
