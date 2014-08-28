package co.com.alianza.infrastructure.anticorruption.recursos

import co.com.alianza.persistence.repositories.RecursosPerfilRepository
import scalaz.Validation
import scala.concurrent.{ExecutionContext, Future}
import co.com.alianza.exceptions.PersistenceException
import co.com.alianza.app.MainActors
import scalaz.{Failure => zFailure, Success => zSuccess}
import co.com.alianza.infrastructure.dto.RecursoUsuario
import co.com.alianza.persistence.entities.{RecursoPerfil => eRecursoUsuario}

object DataAccessAdapter {

  implicit val ec: ExecutionContext = MainActors.dataAccesEx

  def obtenerRecursos(idUsuario : Int): Future[Validation[PersistenceException, List[RecursoUsuario]]] = {
    val repo = new RecursosPerfilRepository()
    repo.obtenerRecursos(idUsuario) map {
      x => transformValidationList(x)
    }
  }

  private def transformValidationList(origin: Validation[PersistenceException, List[eRecursoUsuario]]): Validation[PersistenceException, List[RecursoUsuario]] = {
    origin match {
      case zSuccess(response: List[eRecursoUsuario]) => zSuccess(DataAccessTranslator.translate(response))
      case zFailure(error)    =>  zFailure(error)
    }
  }
}


