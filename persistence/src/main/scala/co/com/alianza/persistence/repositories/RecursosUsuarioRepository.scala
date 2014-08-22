package co.com.alianza.persistence.repositories

import scala.concurrent.{ExecutionContext, Future}
import co.com.alianza.exceptions.PersistenceException

import scala.util.Try
import scalaz.Validation

import co.com.alianza.persistence.entities.{CustomDriver, RecursoUsuarioTable, RecursoUsuario}


import scala.slick.lifted.TableQuery
import CustomDriver.simple._

/**
 * Repositorio para acceder a [[RecursoUsuarioTable]]
 *
 * @author seven4n
 */
class RecursosUsuarioRepository ( implicit executionContext: ExecutionContext) extends AlianzaRepository  {


  val recursos = TableQuery[RecursoUsuarioTable]

  /** Obtiene los recursos relacionados al usuario
    *
    * @return
    */
  def obtenerRecursos(idUsuario : Int): Future[Validation[PersistenceException, List[RecursoUsuario]]] = loan {
    implicit session =>
      val resultTry =  Try { recursos.filter(x => x.idUsuario === idUsuario).list }
      resolveTry(resultTry, "Consulta todos los Recursos por Id Usuario")
  }

}
