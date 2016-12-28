package co.com.alianza.infrastructure.anticorruption.configuraciones

import scalaz.Validation
import scala.concurrent.{ ExecutionContext, Future }
import co.com.alianza.exceptions.PersistenceException

import scalaz.{ Failure => zFailure, Success => zSuccess }
import co.com.alianza.infrastructure.dto.Configuracion
import co.com.alianza.persistence.entities.{ Configuracion => eConfiguraciones }
import co.com.alianza.persistence.repositories.ConfiguracionesRepository
import co.com.alianza.persistence.util.DataBaseExecutionContext

object DataAccessAdapter {

  implicit val ec: ExecutionContext = DataBaseExecutionContext.executionContext

  def obtenerConfiguraciones(): Future[Validation[PersistenceException, List[Configuracion]]] = {
    val repo = new ConfiguracionesRepository()
    repo.obtenerConfiguraciones() map {
      x => transformValidationList(x)
    }
  }

  def obtenerConfiguracionPorLlave(llave: String): Future[Validation[PersistenceException, Option[Configuracion]]] = {
    val repo = new ConfiguracionesRepository()
    repo.obtenerConfiguracionPorLlave(llave) map {
      x => transformValidation(x)
    }
  }

  private def transformValidationList(origin: Validation[PersistenceException, Seq[eConfiguraciones]]): Validation[PersistenceException, List[Configuracion]] = {
    origin match {
      case zSuccess(response: Seq[eConfiguraciones]) => zSuccess(DataAccessTranslator.translateConfiguracion(response))
      case zFailure(error) => zFailure(error)
    }
  }

  private def transformValidation(origin: Validation[PersistenceException, Option[eConfiguraciones]]): Validation[PersistenceException, Option[Configuracion]] = {
    origin match {
      case zSuccess(response) =>
        response match {
          case Some(configuracion) => zSuccess(Some(DataAccessTranslator.translateConfiguracion(configuracion)))
          case _ => zSuccess(None)
        }

      case zFailure(error) => zFailure(error)
    }
  }

}
