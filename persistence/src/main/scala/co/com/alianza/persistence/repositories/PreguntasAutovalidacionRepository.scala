package co.com.alianza.persistence.repositories


import co.com.alianza.exceptions.PersistenceException
import co.com.alianza.persistence.entities.CustomDriver.simple._
import co.com.alianza.persistence.entities.{RespuestasAutovalidacionUsuario, PreguntasAutovalidacion, RespuestasAutovalidacionUsuarioTable, PreguntasAutovalidacionTable}

import scala.concurrent.{ExecutionContext, Future}
import scala.slick.lifted.TableQuery
import scala.util.Try
import scalaz.Validation


/**
 *
 * @author seven4n
 */
class PreguntasAutovalidacionRepository ( implicit executionContext: ExecutionContext) extends AlianzaRepository  {


  val preguntasTable = TableQuery[PreguntasAutovalidacionTable]
  val respuestasUsuarioTable = TableQuery[RespuestasAutovalidacionUsuarioTable]

  def obtenerPreguntas(): Future[Validation[PersistenceException, List[PreguntasAutovalidacion]]] = loan {
    implicit session =>
      val resultTry =  Try { preguntasTable.list }
      resolveTry(resultTry, "Consulta todas las preguntas")
  }

  def guardarRespuestasClienteIndividual(respuestas:List[RespuestasAutovalidacionUsuario]) : Future[Validation[PersistenceException, List[Int]]] = loan {
    implicit session =>
      val resultTry = Try{(respuestasUsuarioTable  ++= respuestas).toList}
      resolveTry(resultTry, "Guardar respuestas de autovalidacion")
  }
}
