package portal.transaccional.autenticacion.service.drivers.cliente

import co.com.alianza.infrastructure.dto.Cliente

import scala.concurrent.Future

/**
 * Created by hernando on 2016
 */
trait ClienteRepository {

  def getCliente(documento: String, tipoIdentificacion: Option[Int]): Future[Cliente]

  def validarEstado(cliente: Cliente): Future[Boolean]
}
