package co.com.alianza.infrastructure.anticorruption.autovalidacion

import co.com.alianza.exceptions.PersistenceException
import co.com.alianza.infrastructure.dto.{Pregunta, Respuesta, RespuestaCompleta}
import co.com.alianza.persistence.entities.{PreguntasAutovalidacion, RespuestasAutovalidacionUsuario}
import co.com.alianza.persistence.repositories.PreguntasAutovalidacionRepository
import co.com.alianza.persistence.util.DataBaseExecutionContext

import scala.concurrent.{ExecutionContext, Future}
import scalaz.{Validation, Failure => zFailure, Success => zSuccess}

object DataAccessAdapter {

  implicit val ec: ExecutionContext = DataBaseExecutionContext.executionContext

  def obtenerPreguntas(): Future[Validation[PersistenceException, List[Pregunta]]] = {
    val repo = new PreguntasAutovalidacionRepository()
    repo.obtenerPreguntas().map(x => transformPreguntaList(x))
  }

  def guardarRespuestasClienteIndividual(respuestas: List[RespuestasAutovalidacionUsuario]): Future[Validation[PersistenceException, List[Int]]] = {
    val repo = new PreguntasAutovalidacionRepository()
    repo.guardarRespuestasClienteIndividual(respuestas)
  }

  def guardarRespuestasClienteAdministrador(respuestas: List[RespuestasAutovalidacionUsuario]): Future[Validation[PersistenceException, List[Int]]] = {
    val repo = new PreguntasAutovalidacionRepository()
    repo.guardarRespuestasClienteAdministrador(respuestas)
  }

  def obtenerPreguntasClienteIndividual(idUsuario: Int): Future[Validation[PersistenceException, List[Pregunta]]] = {
    val repo = new PreguntasAutovalidacionRepository()
    repo.obtenerPreguntasClienteIndividual(idUsuario) map {
      x => toPreguntaList(x)
    }
  }

  def obtenerPreguntasClienteAdministrador(idUsuario: Int): Future[Validation[PersistenceException, List[Pregunta]]] = {
    val repo = new PreguntasAutovalidacionRepository()
    repo.obtenerPreguntasClienteAdministrador(idUsuario) map {
      x => toPreguntaList(x)
    }
  }

  def obtenerRespuestasClienteIndividual(idUsuario: Int): Future[Validation[PersistenceException, List[Respuesta]]] = {
    val repo = new PreguntasAutovalidacionRepository()
    repo.obtenerPreguntasClienteIndividual(idUsuario) map {
      x => toRespuestaList(x)
    }
  }

  def obtenerRespuestasClienteAdministrador(idUsuario: Int): Future[Validation[PersistenceException, List[Respuesta]]] = {
    val repo = new PreguntasAutovalidacionRepository()
    repo.obtenerPreguntasClienteAdministrador(idUsuario) map {
      x => toRespuestaList(x)
    }
  }

  def obtenerRespuestaCompletaClienteIndividual(idUsuario: Int): Future[Validation[PersistenceException, List[RespuestaCompleta]]] = {
    val repo = new PreguntasAutovalidacionRepository()
    repo.obtenerPreguntasClienteIndividual(idUsuario) map {
      x => toRespuestaCompletaList(x)
    }
  }

  def obtenerRespuestaCompletaClienteAdministrador(idUsuario: Int): Future[Validation[PersistenceException, List[RespuestaCompleta]]] = {
    val repo = new PreguntasAutovalidacionRepository()
    repo.obtenerPreguntasClienteAdministrador(idUsuario) map {
      x => toRespuestaCompletaList(x)
    }
  }

  def bloquearRespuestasClienteIndividual(idUsuario: Int): Future[Validation[PersistenceException, Int]] = {
    val repo = new PreguntasAutovalidacionRepository()
    repo.bloquearRespuestasClienteIndividual(idUsuario)
  }

  def bloquearRespuestasClienteAdministrador(idUsuario: Int): Future[Validation[PersistenceException, Int]] = {
    val repo = new PreguntasAutovalidacionRepository()
    repo.bloquearRespuestasClienteAdministrador(idUsuario)
  }

  private def transformPreguntaList(origin: Validation[PersistenceException, Seq[PreguntasAutovalidacion]]): Validation[PersistenceException, List[Pregunta]] = {
    origin match {
      case zSuccess(response: Seq[PreguntasAutovalidacion]) => zSuccess(DataAccessTranslator.translatePregunta(response).toList)
      case zFailure(error) => zFailure(error)
    }
  }

  private def transformRespuestaList(origin: Validation[PersistenceException, List[RespuestasAutovalidacionUsuario]]): Validation[PersistenceException, List[Respuesta]] = {

    origin match {
      case zSuccess(response: List[RespuestasAutovalidacionUsuario]) => zSuccess(DataAccessTranslator.translateRespuesta(response))
      case zFailure(error) => zFailure(error)
    }
  }

  private def toPreguntaList(origin: Validation[PersistenceException, Seq[(PreguntasAutovalidacion, RespuestasAutovalidacionUsuario)]]): Validation[PersistenceException, List[Pregunta]] = {
    origin match {
      case zSuccess(response: Seq[(PreguntasAutovalidacion, RespuestasAutovalidacionUsuario)]) => zSuccess(DataAccessTranslator.toPreguntaList(response))
      case zFailure(error) => zFailure(error)
    }
  }

  private def toRespuestaList(origin: Validation[PersistenceException, Seq[(PreguntasAutovalidacion, RespuestasAutovalidacionUsuario)]]): Validation[PersistenceException, List[Respuesta]] = {
    origin match {
      case zSuccess(response: Seq[(PreguntasAutovalidacion, RespuestasAutovalidacionUsuario)]) => zSuccess(DataAccessTranslator.toRespuestaList(response))
      case zFailure(error) => zFailure(error)
    }
  }

  private def toRespuestaCompletaList(origin: Validation[PersistenceException, Seq[(PreguntasAutovalidacion, RespuestasAutovalidacionUsuario)]]): Validation[PersistenceException, List[RespuestaCompleta]] = {
    origin match {
      case zSuccess(response: Seq[(PreguntasAutovalidacion, RespuestasAutovalidacionUsuario)]) => zSuccess(DataAccessTranslator.toRespuestaCompletaList(response))
      case zFailure(error) => zFailure(error)
    }
  }

}
