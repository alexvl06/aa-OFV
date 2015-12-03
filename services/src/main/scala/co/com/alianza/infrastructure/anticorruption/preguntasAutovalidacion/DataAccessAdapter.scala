package co.com.alianza.infrastructure.anticorruption.preguntasAutovalidacion

import co.com.alianza.app.MainActors
import co.com.alianza.exceptions.PersistenceException
import co.com.alianza.infrastructure.dto.Pregunta
import co.com.alianza.persistence.entities.{PreguntasConfrontacionUsuario, PreguntasConfrontacionAutovalidacion}
import co.com.alianza.persistence.repositories.PreguntasAutovalidacionRepository

import scala.concurrent.{ExecutionContext, Future}
import scalaz.{Failure => zFailure, Success => zSuccess, Validation}

object DataAccessAdapter {

  implicit val ec: ExecutionContext = MainActors.dataAccesEx

  def obtenerPreguntas(): Future[Validation[PersistenceException, List[Pregunta]]] = {
    val repo = new PreguntasAutovalidacionRepository()
    repo.obtenerPreguntas() map {
      x => transformValidationList(x)
    }
  }

  def guardarRespuestas (respuestas:List[PreguntasConfrontacionUsuario]): Future[Validation[PersistenceException, List[Int]]] = {
    val repo = new PreguntasAutovalidacionRepository()
    repo.guardarRespuestas(respuestas)
  }

  private def transformValidationList(origin: Validation[PersistenceException, List[PreguntasConfrontacionAutovalidacion]]): Validation[PersistenceException, List[Pregunta]] = {
    origin match {
      case zSuccess(response: List[PreguntasConfrontacionAutovalidacion]) => zSuccess(DataAccessTranslator.translatePregunta(response))
      case zFailure(error)    =>  zFailure(error)
    }
  }

}