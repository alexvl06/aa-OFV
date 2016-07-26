package co.com.alianza.infrastructure.anticorruption.recursos

import co.com.alianza.persistence.repositories.RecursosPerfilRepository

import scalaz.Validation
import scala.concurrent.{ ExecutionContext, Future }
import co.com.alianza.exceptions.PersistenceException

import scalaz.{ Failure => zFailure, Success => zSuccess }
import co.com.alianza.infrastructure.dto.RecursoUsuario
import co.com.alianza.persistence.entities.{ RecursoPerfil => eRecursoPerfil }
import co.com.alianza.persistence.util.DataBaseExecutionContext

object DataAccessAdapter {

  implicit val ec: ExecutionContext = DataBaseExecutionContext.executionContext

  def obtenerRecursos(idUsuario: Int): Future[Validation[PersistenceException, List[RecursoUsuario]]] = {
    val repo = new RecursosPerfilRepository()
    repo.obtenerRecursos(idUsuario) map {
      x => transformValidationList(x)
    }
  }

  private def transformValidationList(origin: Validation[PersistenceException, Seq[eRecursoPerfil]]): Validation[PersistenceException, List[RecursoUsuario]] = {
    origin match {
      case zSuccess(response: Seq[eRecursoPerfil]) => zSuccess(DataAccessTranslator.translate(response))
      case zFailure(error) => zFailure(error)
    }
  }
}

