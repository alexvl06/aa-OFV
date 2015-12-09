package co.com.alianza.persistence.repositories


import co.com.alianza.exceptions.PersistenceException
import co.com.alianza.persistence.entities.CustomDriver.simple._
import co.com.alianza.persistence.entities._

import scala.concurrent.{ExecutionContext, Future}
import scala.slick.lifted.TableQuery
import scala.util.{Random, Try}
import scalaz.Validation


/**
 *
 * @author seven4n
 */
class PreguntasAutovalidacionRepository ( implicit executionContext: ExecutionContext) extends AlianzaRepository  {


  val preguntasTable = TableQuery[PreguntasAutovalidacionTable]
  val respuestasUsuarioTable = TableQuery[RespuestasAutovalidacionUsuarioTable]
  val respuestasClienteAdministradorTable = TableQuery[RespuestasAutovalidacionClienteAdministradorTable]

  def obtenerPreguntas(): Future[Validation[PersistenceException, List[PreguntasAutovalidacion]]] = loan {
    implicit session =>
      val resultTry =  Try { preguntasTable.list }
      resolveTry(resultTry, "Consulta todas las preguntas")
  }

  def guardarRespuestasClienteIndividual(respuestas:List[RespuestasAutovalidacionUsuario]) : Future[Validation[PersistenceException, List[Int]]] = loan {
    implicit session =>
      val resultTry = Try{
        respuestasUsuarioTable.filter(respuestas => respuestas.idUsuario === respuestas.idUsuario ).delete
        (respuestasUsuarioTable  ++= respuestas).toList
      }
      resolveTry(resultTry, "Guardar respuestas de autovalidacion para cliente individual")
  }

  def guardarRespuestasClienteAdministrador(respuestas:List[RespuestasAutovalidacionUsuario]) : Future[Validation[PersistenceException, List[Int]]] = loan {
    implicit session =>
      val resultTry = Try{
        respuestasClienteAdministradorTable.filter(respuestas => respuestas.idUsuario === respuestas.idUsuario ).delete
        (respuestasClienteAdministradorTable  ++= respuestas).toList
      }
      resolveTry(resultTry, "Guardar respuestas de autovalidacion para cliente administrador")
  }

  def obtenerPreguntasRandomClienteIndividual( idUsuario: Int ) : Future[Validation[PersistenceException, List[PreguntasAutovalidacion]]] = loan {
    implicit session =>
      val respuestaJoin = for {
        ((pregunta, respuesta)) <- preguntasTable innerJoin respuestasUsuarioTable on (_.id === _.idPregunta)
        if respuesta.idUsuario === idUsuario
      } yield pregunta

      val resultTry = Try{ Random.shuffle(respuestaJoin.list).take(3) }
      resolveTry(resultTry, "Obtener 3 preguntas al azar")
  }
}
