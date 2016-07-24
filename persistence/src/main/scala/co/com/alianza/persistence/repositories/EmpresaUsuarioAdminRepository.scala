package co.com.alianza.persistence.repositories

import co.com.alianza.exceptions.PersistenceException
import co.com.alianza.persistence.entities.CustomDriver.simple._
import co.com.alianza.persistence.entities._
import slick.lifted.TableQuery

import scala.concurrent.{ ExecutionContext, Future }
import scalaz.{ Validation, Failure => zFailure, Success => zSuccess }

/**
 * Created by david on 12/06/14.
 */
class EmpresaUsuarioAdminRepository(implicit executionContext: ExecutionContext) extends AlianzaRepository {

  val empresaUsuarioAdmin = TableQuery[EmpresaUsuarioAdminTable]

  def obtenerIdEmpresa(idUsuario: Int): Future[Validation[PersistenceException, Int]] = loan {
    implicit session =>
      val resultTry = session.database.run(empresaUsuarioAdmin.filter(_.idUsuario === idUsuario).map(_.idEmpresa).result.head)
      resolveTry(resultTry, "Consulta id empresa por idUsuario empresarial admin")
  }

}