package co.com.alianza.infrastructure.anticorruption.actualizacion

import scalaz.Validation
import scala.concurrent.{ExecutionContext, Future}
import co.com.alianza.exceptions.PersistenceException
import co.com.alianza.app.MainActors
import scalaz.{Failure => zFailure, Success => zSuccess}
import co.com.alianza.infrastructure.dto.{Pais, DatosCliente, Cliente}
import co.com.alianza.persistence.repositories.core.{ActualizacionRepository}

object DataAccessAdapter {

  implicit val ec: ExecutionContext = MainActors.dataAccesEx

  def consultaPaises = {
    val repo = new ActualizacionRepository()
    repo.listarPaises map { x => transformValidationPais(x) }
  }

  private def transformValidationPais(origin: Validation[PersistenceException, String]): Validation[PersistenceException, Option[List[Pais]]] = {
    origin match {
      case zSuccess(response: String) => zSuccess(DataAccessTranslator.translatePaises(response))
      case zFailure(error)            => zFailure(error)
    }
  }

}