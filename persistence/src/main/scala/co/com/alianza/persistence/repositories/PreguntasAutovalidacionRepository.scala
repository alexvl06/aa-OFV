package co.com.alianza.persistence.repositories

import co.com.alianza.exceptions.PersistenceException
import co.com.alianza.persistence.entities.CustomDriver.simple._
import co.com.alianza.persistence.entities._
import slick.dbio.DBIOAction
import slick.dbio.Effect.Write

import scala.concurrent.{ ExecutionContext, Future }
import slick.lifted.TableQuery

import scala.util.Try
import scalaz.Validation

/**
 *
 * @author seven4n
 */
class PreguntasAutovalidacionRepository(implicit executionContext: ExecutionContext) extends AlianzaRepository {

  val preguntasTable = TableQuery[PreguntasAutovalidacionTable]
  val respuestasUsuarioTable = TableQuery[RespuestasAutovalidacionUsuarioTable]
  val respuestasClienteAdministradorTable = TableQuery[RespuestasAutovalidacionUsuarioAdministradorTable]

  def obtenerPreguntas(): Future[Validation[PersistenceException, Seq[PreguntasAutovalidacion]]] = loan {
    implicit session =>
      val resultTry = session.database.run(preguntasTable.result)
      resolveTry(resultTry, "Consulta todas las preguntas")
  }

  def guardarRespuestasClienteIndividual(respuestas: List[RespuestasAutovalidacionUsuario]): Future[Validation[PersistenceException, List[Int]]] = loan {

    implicit session =>
      val resultTry = for {
        delete <- session.database.run(respuestasUsuarioTable.filter(res => res.idUsuario === respuestas.head.idUsuario).delete)
        insert <- session.database.run(respuestasUsuarioTable ++= respuestas)
      } yield List(delete)

      resolveTry(resultTry, "Guardar respuestas de autovalidacion para cliente individual")
  }

  def guardarRespuestasClienteAdministrador(respuestas: List[RespuestasAutovalidacionUsuario]): Future[Validation[PersistenceException, List[Int]]] = loan {
    implicit session =>

      val resultTry = for {
        delete <- session.database.run(respuestasClienteAdministradorTable.filter(res => res.idUsuario === respuestas.head.idUsuario).delete)
        insert <- session.database.run(respuestasClienteAdministradorTable ++= respuestas)
      } yield List(delete)

      resolveTry(resultTry, "Guardar respuestas de autovalidacion para cliente administrador")
  }

  def obtenerPreguntasClienteIndividual(idUsuario: Int): Future[Validation[PersistenceException, Seq[(PreguntasAutovalidacion, RespuestasAutovalidacionUsuario)]]] = loan {
    implicit session =>
      val respuestaJoin = for {
        (pregunta, respuesta) <- preguntasTable join respuestasUsuarioTable on (_.id === _.idPregunta)
        if respuesta.idUsuario === idUsuario
      } yield (pregunta, respuesta)

      val resultTry = session.database.run(respuestaJoin.result)
      resolveTry(resultTry, "Obtener las preguntas definidas del cliente individual")
  }

  def obtenerPreguntasClienteAdministrador(idUsuario: Int): Future[Validation[PersistenceException, Seq[(PreguntasAutovalidacion, RespuestasAutovalidacionUsuario)]]] = loan {
    implicit session =>
      val respuestaJoin = for {
        (pregunta, respuesta) <- preguntasTable join respuestasClienteAdministradorTable on (_.id === _.idPregunta)
        if respuesta.idUsuario === idUsuario
      } yield (pregunta, respuesta)
      val resultTry = session.database.run(respuestaJoin.result)
      resolveTry(resultTry, "Obtener las preguntas definidas del cliente individual")
  }

  def bloquearRespuestasClienteIndividual(idUsuario: Int): Future[Validation[PersistenceException, Int]] = loan {
    implicit session =>
      val resultTry = session.database.run(respuestasUsuarioTable.filter(x => x.idUsuario === idUsuario).delete)
      resolveTry(resultTry, "Eliminar repsuestas del Usuario")
  }

  def bloquearRespuestasClienteAdministrador(idUsuario: Int): Future[Validation[PersistenceException, Int]] = loan {
    implicit session =>
      val resultTry = session.database.run(respuestasClienteAdministradorTable.filter(x => x.idUsuario === idUsuario).delete)
      resolveTry(resultTry, "Eliminar repsuestas del Usuario")
  }

}
