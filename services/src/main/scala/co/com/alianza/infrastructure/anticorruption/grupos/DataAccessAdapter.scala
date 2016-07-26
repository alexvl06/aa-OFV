package co.com.alianza.infrastructure.anticorruption.grupos

import co.com.alianza.exceptions.PersistenceException
import co.com.alianza.infrastructure.dto.Cliente
import co.com.alianza.persistence.repositories.core.ClienteRepository
import co.com.alianza.persistence.util.DataBaseExecutionContext

import scala.concurrent.{ExecutionContext, Future}
import scalaz.{Validation, Failure => zFailure, Success => zSuccess}

object DataAccessAdapter {

  implicit val ec: ExecutionContext = DataBaseExecutionContext.executionContext

  def consultarGrupo(idGrupo: Int): Future[Validation[PersistenceException, Option[Cliente]]] = {

    val repo = new ClienteRepository()
    repo consultaGrupo idGrupo map { x => transformValidationGrupo(x) }
  }

  private def transformValidationGrupo(origin: Validation[PersistenceException, String]): Validation[PersistenceException, Option[Cliente]] = {
    origin match {
      case zSuccess(response: String) =>
        zSuccess(DataAccessTranslator.translateCliente(response))
      case zFailure(error) => zFailure(error)
    }
  }

}

