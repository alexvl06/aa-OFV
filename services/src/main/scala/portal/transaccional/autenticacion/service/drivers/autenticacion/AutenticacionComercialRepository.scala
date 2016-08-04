package portal.transaccional.autenticacion.service.drivers.autenticacion

import scala.concurrent.Future

trait AutenticacionComercialRepository {

  def autenticar(usuario: String, tipoUsuario: Int, contrasena: String): Future[String]

  def autenticarValores: Future[String]

  def autenticarFiduciaria: Future[String]

  def autenticarAdministrador: Future[String]

}
