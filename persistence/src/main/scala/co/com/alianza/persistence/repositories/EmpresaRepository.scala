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
class EmpresaRepository(implicit executionContext: ExecutionContext) extends AlianzaRepository {

  val empresa = TableQuery[EmpresaTable]

  def obtenerEmpresa(nit: String): Future[Validation[PersistenceException, Option[Empresa]]] = loan {
    implicit session =>
      val resultTry = session.database.run(empresa.filter(_.nit === nit).result.headOption)
      resolveTry(resultTry, "Consulta empresa por nit")
  }

}
