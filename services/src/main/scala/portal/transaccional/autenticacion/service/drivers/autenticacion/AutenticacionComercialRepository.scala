package portal.transaccional.autenticacion.service.drivers.autenticacion


import scala.concurrent.Future

trait AutenticacionComercialRepository {

  def autenticar(tipoUsuario: Int, usuario: String, contrasena: String, ip: String ): Future[String]

  def autenticarValores : Future[String]

  def autenticarFiduciaria : Future[String]

  def autenticarAdministrador : Future[String]

}
