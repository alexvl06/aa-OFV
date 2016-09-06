package portal.transaccional.autenticacion.service.drivers.cliente

import co.com.alianza.infrastructure.dto.Cliente

import scala.concurrent.Future

/**
 * Created by hernando on 25/07/16.
 */
trait ClienteRepository {

  def getCliente(documento: String, tipoIdentificacion: Option[Int]): Future[Cliente]

  def validarEstado(cliente: Cliente): Future[Boolean]

  def validarFidInmobiliarios(numDocumento : String): Future[Boolean]

}
