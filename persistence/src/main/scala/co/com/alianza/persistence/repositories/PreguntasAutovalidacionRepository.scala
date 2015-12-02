package co.com.alianza.persistence.repositories


import co.com.alianza.exceptions.PersistenceException
import co.com.alianza.persistence.entities.CustomDriver.simple._
import co.com.alianza.persistence.entities.{PreguntasConfrontacionUsuario, PreguntasConfrontacionAutovalidacion, PreguntasConfrontacionUsuarioTable, PreguntasConfrontacionAutovalidacionTable}

import scala.concurrent.{ExecutionContext, Future}
import scala.slick.lifted.TableQuery
import scala.util.Try
import scalaz.Validation

/**
 *
 * @author seven4n
 */
class PreguntasAutovalidacionRepository ( implicit executionContext: ExecutionContext) extends AlianzaRepository  {


  val preguntasTable = TableQuery[PreguntasConfrontacionAutovalidacionTable]
  val respuestasUsuarioTable = TableQuery[PreguntasConfrontacionUsuarioTable]

  def obtenerPreguntas(): Future[Validation[PersistenceException, List[PreguntasConfrontacionAutovalidacion]]] = loan {
    implicit session =>
      val resultTry =  Try { preguntasTable.list }
      resolveTry(resultTry, "Consulta todas las preguntas")
  }

  def guardarRespuestas(respuestas:List[PreguntasConfrontacionUsuario]) : Future[Validation[PersistenceException, List[Int]]] = loan {

    implicit session =>
      val resultTry = Try{(respuestasUsuarioTable  ++= respuestas).toList}
      resolveTry(resultTry, "Guardar respuestas de confrontacion")
  }
}
