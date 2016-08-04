package portal.transaccional.autenticacion.service.drivers.cliente

import co.com.alianza.exceptions.ValidacionException
import co.com.alianza.infrastructure.anticorruption.clientes.DataAccessTranslator
import co.com.alianza.infrastructure.dto.Cliente
import enumerations.EstadosCliente
import portal.transaccional.fiduciaria.autenticacion.storage.daos.core.ClienteDAOs

import scala.concurrent.{ Future, ExecutionContext }

/**
 * Created by hernando on 25/07/16.
 */
case class ClienteDriverCoreRepository(clienteCoreRepo: ClienteDAOs)(implicit val ex: ExecutionContext) extends ClienteRepository {

  def getCliente(documento: String): Future[Cliente] = {

    println("VALIDACION 2");
    for {
      clienteString <- clienteCoreRepo.consultaCliente(documento)
      clienteOption <- Future { DataAccessTranslator.translateCliente(clienteString) }
      cliente <- validarCliente(clienteOption)
    } yield cliente
  }

  private def validarCliente(clienteOption: Option[Cliente]): Future[Cliente] = {
    clienteOption match {
      case Some(cliente: Cliente) => Future.successful(cliente)
      case _ => Future.failed(ValidacionException("401.2", "No existe cliente core"))
    }
  }

  def validarEstado(cliente: Cliente): Future[Boolean] = {
    println("VALIDACION 3");
    if (cliente.wcli_estado != EstadosCliente.inactivo && cliente.wcli_estado != EstadosCliente.bloqueado &&
      cliente.wcli_estado != EstadosCliente.activo)
      Future.failed(ValidacionException("401.1", "Cliente inactivo core"))
    else Future.successful(true)
  }

}
