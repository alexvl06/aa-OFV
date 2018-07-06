package portal.transaccional.autenticacion.service.drivers.autorizacion

import scala.concurrent.Future

trait AutorizacionOFVRepository {
  def validar(token: String, tipoCliente: String): Future[Boolean]
}
