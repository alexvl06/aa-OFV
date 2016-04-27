package co.com.alianza.persistence.repositories

import co.com.alianza.exceptions.PersistenceException
import co.com.alianza.persistence.entities.CustomDriver.simple._
import co.com.alianza.persistence.entities._

import scala.concurrent.{ ExecutionContext, Future }
import scala.slick.jdbc.JdbcBackend.Session
import scala.slick.lifted.TableQuery
import scala.util.Try
import scalaz.{ Validation, Failure => zFailure, Success => zSuccess }

/**
 * Created by david on 12/06/14.
 */
class EmpresaUsuarioAdminRepository(implicit executionContext: ExecutionContext) extends AlianzaRepository {

  val empresaUsuarioAdmin = TableQuery[EmpresaUsuarioAdminTable]

  def obtenerIdEmpresa(idUsuario: Int): Future[Validation[PersistenceException, Int]] = loan {
    implicit session =>
      val resultTry = Try { empresaUsuarioAdmin.filter(_.idUsuario === idUsuario).list.headOption.get.idEmpresa }
      resolveTry(resultTry, "Consulta id empresa por idUsuario empresarial admin")
  }

}