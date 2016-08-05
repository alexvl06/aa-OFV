package portal.transaccional.autenticacion.service.drivers.autenticacion

import scala.concurrent.Future

trait AutenticacionComercialRepository {

  def autenticar(usuario: String, tipoUsuario: Int, contrasena: String, ip: String): Future[String]

  def autenticarComercial(usuario: String, tipoUsuario: Int, password: String, ip: String): Future[String]

  def autenticarAdministrador: Future[String]

}
