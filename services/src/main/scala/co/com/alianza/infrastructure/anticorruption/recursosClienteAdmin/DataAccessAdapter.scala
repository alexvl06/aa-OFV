package co.com.alianza.infrastructure.anticorruption.recursosClienteAdmin

import co.com.alianza.persistence.repositories.RecursoPerfilClienteAdminRepository
import scalaz.Validation
import scala.concurrent.{ ExecutionContext, Future }
import co.com.alianza.exceptions.PersistenceException
import co.com.alianza.app.MainActors
import scalaz.{ Failure => zFailure, Success => zSuccess }
import co.com.alianza.infrastructure.dto.RecursoPerfilClienteAdmin
import co.com.alianza.persistence.entities.{ RecursoPerfilClienteAdmin => eRecursoPerfilClienteAdmin }

/**
 * Created by manuel on 3/02/15.
 */
object DataAccessAdapter {

  implicit val ec: ExecutionContext = MainActors.dataAccesEx

  def obtenerRecursos(idUsuario: Int): Future[Validation[PersistenceException, List[RecursoPerfilClienteAdmin]]] =
    new RecursoPerfilClienteAdminRepository obtenerRecursosPerfiles (idUsuario) map transformValidationList

  private def transformValidationList(origin: Validation[PersistenceException, List[eRecursoPerfilClienteAdmin]]): Validation[PersistenceException, List[RecursoPerfilClienteAdmin]] = {
    origin match {
      case zSuccess(response: List[eRecursoPerfilClienteAdmin]) => zSuccess(DataAccessTranslator.translate(response))
      case zFailure(error) => zFailure(error)
    }
  }
}
