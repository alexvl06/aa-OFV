package co.com.alianza.infrastructure.anticorruption.clientes

import scalaz.Validation
import scala.concurrent.{ExecutionContext, Future}
import co.com.alianza.exceptions.PersistenceException
import co.com.alianza.app.MainActors
import scalaz.{Failure => zFailure, Success => zSuccess}
import co.com.alianza.infrastructure.dto.Cliente
import co.com.alianza.persistence.messages.ConsultaClienteRequest
import co.com.alianza.persistence.repositories.core.ClienteRepository

object DataAccessAdapter {

  implicit val ec: ExecutionContext = MainActors.dataAccesEx

  def consultarCliente( message:ConsultaClienteRequest ): Future[Validation[PersistenceException, Option[Cliente]]] = {
    val repo = new ClienteRepository()
    repo consultaCliente message map { x => transformValidation(x) }
  }

  private def transformValidation(origin: Validation[PersistenceException, String]): Validation[PersistenceException, Option[Cliente]] = {
    origin match {
      case zSuccess(response: String) =>  zSuccess(DataAccessTranslator.translateCliente(response))
      case zFailure(error)    =>  zFailure(error)
    }
  }

}


