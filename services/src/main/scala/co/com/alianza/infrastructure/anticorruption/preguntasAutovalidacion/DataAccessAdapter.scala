package co.com.alianza.infrastructure.anticorruption.preguntasAutovalidacion

import co.com.alianza.app.MainActors
import co.com.alianza.exceptions.PersistenceException
import co.com.alianza.infrastructure.dto.{RespuestaCompleta, Respuesta, Pregunta}
import co.com.alianza.persistence.entities.{RespuestasAutovalidacionUsuario, PreguntasAutovalidacion}
import co.com.alianza.persistence.repositories.PreguntasAutovalidacionRepository

import scala.concurrent.{ExecutionContext, Future}
import scalaz.{Failure => zFailure, Success => zSuccess, Validation}

object DataAccessAdapter {

  implicit val ec: ExecutionContext = MainActors.dataAccesEx

  def obtenerPreguntas(): Future[Validation[PersistenceException, List[Pregunta]]] = {
    val repo = new PreguntasAutovalidacionRepository()
    repo.obtenerPreguntas() map {
      x => transformPreguntaList(x)
    }
  }

  def guardarRespuestas (respuestas:List[RespuestasAutovalidacionUsuario]): Future[Validation[PersistenceException, List[Int]]] = {
    val repo = new PreguntasAutovalidacionRepository()
    repo.guardarRespuestasClienteIndividual(respuestas)
  }

  def guardarRespuestasClienteAdministrador (respuestas:List[RespuestasAutovalidacionUsuario]): Future[Validation[PersistenceException, List[Int]]] = {
    val repo = new PreguntasAutovalidacionRepository()
    repo.guardarRespuestasClienteAdministrador(respuestas)
  }

  def obtenerPreguntasClienteIndividual(idUsuario: Option[Int]): Future[Validation[PersistenceException, List[Pregunta]]] = {
    val repo = new PreguntasAutovalidacionRepository()
    repo.obtenerPreguntasClienteIndividual(idUsuario.get) map {
      x => toPreguntaList(x)
    }
  }

  def obtenerRespuestasClienteIndividual(idUsuario: Option[Int]): Future[Validation[PersistenceException, List[Respuesta]]] = {
    val repo = new PreguntasAutovalidacionRepository()
    repo.obtenerPreguntasClienteIndividual(idUsuario.get) map {
      x => toRespuestaList(x)
    }
  }

  def obtenerRespuestaCompletaClienteIndividual(idUsuario: Option[Int]): Future[Validation[PersistenceException, List[RespuestaCompleta]]] = {
    val repo = new PreguntasAutovalidacionRepository()
    repo.obtenerPreguntasClienteIndividual(idUsuario.get) map {
      x => toRespuestaCompletaList(x)
    }
  }

  private def transformPreguntaList(origin: Validation[PersistenceException, List[PreguntasAutovalidacion]]): Validation[PersistenceException, List[Pregunta]] = {
    origin match {
      case zSuccess(response: List[PreguntasAutovalidacion]) => zSuccess(DataAccessTranslator.translatePregunta(response))
      case zFailure(error)    =>  zFailure(error)
    }
  }

  private def transformRespuestaList(origin: Validation[PersistenceException, List[RespuestasAutovalidacionUsuario]]): Validation[PersistenceException, List[Respuesta]] = {
    origin match {
      case zSuccess(response: List[RespuestasAutovalidacionUsuario]) => zSuccess(DataAccessTranslator.translateRespuesta(response))
      case zFailure(error)    =>  zFailure(error)
    }
  }

  private def toPreguntaList(origin: Validation[PersistenceException, List[(PreguntasAutovalidacion, RespuestasAutovalidacionUsuario)]]): Validation[PersistenceException, List[Pregunta]] = {
    origin match {
      case zSuccess(response: List[(PreguntasAutovalidacion, RespuestasAutovalidacionUsuario)]) => zSuccess(DataAccessTranslator.toPreguntaList(response))
      case zFailure(error)    =>  zFailure(error)
    }
  }

  private def toRespuestaList(origin: Validation[PersistenceException, List[(PreguntasAutovalidacion, RespuestasAutovalidacionUsuario)]]): Validation[PersistenceException, List[Respuesta]] = {
    origin match {
      case zSuccess(response: List[(PreguntasAutovalidacion, RespuestasAutovalidacionUsuario)]) => zSuccess(DataAccessTranslator.toRespuestaList(response))
      case zFailure(error)    =>  zFailure(error)
    }
  }

  private def toRespuestaCompletaList(origin: Validation[PersistenceException, List[(PreguntasAutovalidacion, RespuestasAutovalidacionUsuario)]]): Validation[PersistenceException, List[RespuestaCompleta]] = {
    origin match {
      case zSuccess(response: List[(PreguntasAutovalidacion, RespuestasAutovalidacionUsuario)]) => zSuccess(DataAccessTranslator.toRespuestaCompletaList(response))
      case zFailure(error)    =>  zFailure(error)
    }
  }

}