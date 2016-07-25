package portal.transaccional.autenticacion.service.drivers.autenticacion

import scala.concurrent.Future

/**
 * Created by hernando on 25/07/16.
 */
trait AutenticacionRepository {

  def autenticar(tipoIdentificacion: Int, numeroIdentificacion: String, password: String, clientIp: String): Future[String]

  def autenticarUsuarioEmpresa(
    tipoIdentificacion: Int,
    numeroIdentificacion: String,
    usuario: String,
    password: String,
    clientIp: String
  ): Future[String]

}
