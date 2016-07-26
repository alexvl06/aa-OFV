package co.com.alianza.infrastructure.anticorruption.empresa

import co.com.alianza.exceptions.PersistenceException
import co.com.alianza.persistence.entities.Empresa
import co.com.alianza.persistence.repositories.EmpresaRepository
import co.com.alianza.persistence.util.DataBaseExecutionContext

import scala.concurrent.{ExecutionContext, Future}
import scalaz.Validation

/**
 * Created by david on 27/03/15.
 */
object DataAccessAdapter {

  implicit val ec: ExecutionContext = DataBaseExecutionContext.executionContext

  def obtenerEmpresa(nit: String): Future[Validation[PersistenceException, Option[Empresa]]] = {
    val repo = new EmpresaRepository()
    repo.obtenerEmpresa(nit)
  }
}
