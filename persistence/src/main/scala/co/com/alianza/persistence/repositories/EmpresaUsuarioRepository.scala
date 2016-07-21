package co.com.alianza.persistence.repositories

import co.com.alianza.exceptions.PersistenceException
import co.com.alianza.persistence.entities.CustomDriver.simple._
import co.com.alianza.persistence.entities._

import scala.concurrent.{ ExecutionContext, Future }
import slick.jdbc.JdbcBackend.Session
import slick.lifted.TableQuery
import scala.util.Try
import scalaz.{ Validation, Failure => zFailure, Success => zSuccess }

/**
 * Created by david on 12/06/14.
 */
class EmpresaUsuarioRepository(implicit executionContext: ExecutionContext) extends AlianzaRepository {

  val empresaUsuario = TableQuery[EmpresaUsuarioTable]

  def obtenerIdEmpresa(idUsuario: Int): Future[Validation[PersistenceException, Int]] = loan {
    implicit session =>
      val resultTry = Try { empresaUsuario.filter(_.idUsuario === idUsuario).list.headOption.get.idEmpresa }
      resolveTry(resultTry, "Consulta id empresa por idUsuario empresarial")
  }

}
