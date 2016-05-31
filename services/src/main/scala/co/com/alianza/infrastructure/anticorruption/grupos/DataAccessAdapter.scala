package co.com.alianza.infrastructure.anticorruption.grupos

import co.com.alianza.app.MainActors
import co.com.alianza.exceptions.PersistenceException
import co.com.alianza.infrastructure.dto.Cliente
import co.com.alianza.persistence.repositories.core.ClienteRepository

import scala.concurrent.{ ExecutionContext, Future }
import scalaz.{ Failure => zFailure, Success => zSuccess, Validation }

object DataAccessAdapter {

  implicit val ec: ExecutionContext = MainActors.dataAccesEx

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

