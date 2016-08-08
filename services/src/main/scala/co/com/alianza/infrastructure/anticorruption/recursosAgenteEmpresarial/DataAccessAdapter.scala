package co.com.alianza.infrastructure.anticorruption.recursosAgenteEmpresarial

import co.com.alianza.persistence.repositories.RecursoPerfilAgenteRepository

import scalaz.Validation
import scala.concurrent.{ ExecutionContext, Future }
import co.com.alianza.exceptions.PersistenceException

import scalaz.{ Failure => zFailure, Success => zSuccess }
import co.com.alianza.infrastructure.dto.RecursoPerfilAgente
import co.com.alianza.persistence.entities.{ RecursoPerfilAgente => eRecursoPerfilAgente }
import co.com.alianza.persistence.util.DataBaseExecutionContext
/**
 * Created by manuel on 3/02/15.
 */
object DataAccessAdapter {

  implicit val ec: ExecutionContext = DataBaseExecutionContext.executionContext

  def obtenerRecursos(idUsuario: Int): Future[Validation[PersistenceException, List[RecursoPerfilAgente]]] =
    new RecursoPerfilAgenteRepository obtenerRecursosPerfiles (idUsuario) map transformValidationList

  private def transformValidationList(origin: Validation[PersistenceException, Seq[eRecursoPerfilAgente]]): Validation[PersistenceException, List[RecursoPerfilAgente]] = {
    origin match {
      case zSuccess(response: Seq[eRecursoPerfilAgente]) => zSuccess(DataAccessTranslator.translate(response))
      case zFailure(error) => zFailure(error)
    }
  }
}
