package portal.transaccional.autenticacion.service.drivers.cliente

import co.com.alianza.exceptions.ValidacionException
import co.com.alianza.infrastructure.anticorruption.clientes.DataAccessTranslator
import co.com.alianza.infrastructure.dto.Cliente
import enumerations.{ EstadosCliente, TipoIdentificacion }
import portal.transaccional.fiduciaria.autenticacion.storage.daos.core.ClienteDAO

import scala.concurrent.{ ExecutionContext, Future }

/**
 * Created by S4N on 2016
 */
case class ClienteDriverCoreRepository(clienteCoreRepo: ClienteDAO)(implicit val ex: ExecutionContext) extends ClienteRepository {

  def getCliente(documento: String, tipoIdentificacion: Option[Int]): Future[Cliente] = {
    val esGrupo: Boolean = tipoIdentificacion.getOrElse(0) == TipoIdentificacion.GRUPO.identificador
    val consultaClienteCore = esGrupo match {
      case true =>
        clienteCoreRepo.consultaGrupo(documento)
      case _ => clienteCoreRepo.consultaCliente(documento)
    }

    def translateUser(clienteString: String) = Future {
      if (esGrupo) {
        DataAccessTranslator.translateGrupo(clienteString)
      } else {
        DataAccessTranslator.translateCliente(clienteString)
      }
    }

    for {
      clienteString <- consultaClienteCore
      clienteOption <- translateUser(clienteString)
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
    if (cliente.wcli_estado != EstadosCliente.inactivo && cliente.wcli_estado != EstadosCliente.bloqueado &&
      cliente.wcli_estado != EstadosCliente.activo)
      Future.failed(ValidacionException("401.1", "Cliente inactivo core"))
    else Future.successful(true)
  }

}
