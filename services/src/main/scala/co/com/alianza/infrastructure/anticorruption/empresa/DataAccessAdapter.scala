package co.com.alianza.infrastructure.anticorruption.empresa

import co.com.alianza.app.MainActors
import co.com.alianza.exceptions.PersistenceException
import co.com.alianza.persistence.entities.Empresa
import co.com.alianza.persistence.repositories.EmpresaRepository

import scala.concurrent.{ Future, ExecutionContext }
import scalaz.Validation

/**
 * Created by david on 27/03/15.
 */
object DataAccessAdapter {
  implicit val ec: ExecutionContext = MainActors.dataAccesEx

  def obtenerEmpresa(nit: String): Future[Validation[PersistenceException, Option[Empresa]]] = {
    val repo = new EmpresaRepository()
    repo.obtenerEmpresa(nit)
  }
}
