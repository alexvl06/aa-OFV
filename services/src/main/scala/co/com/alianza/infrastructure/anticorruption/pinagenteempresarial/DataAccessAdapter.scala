package co.com.alianza.infrastructure.anticorruption.pinagenteempresarial

import co.com.alianza.exceptions.PersistenceException
import co.com.alianza.persistence.entities.PinAgente
import co.com.alianza.persistence.repositories.PinUsuarioAgenteEmpresarialRepository
import co.com.alianza.persistence.util.DataBaseExecutionContext

import scala.concurrent.{ ExecutionContext, Future }
import scalaz.{ Validation, Failure => zFailure, Success => zSuccess }

/**
 * Created by manuel on 6/01/15.
 */
object DataAccessAdapter {

  implicit val ec: ExecutionContext = DataBaseExecutionContext.executionContext
  val repo = new PinUsuarioAgenteEmpresarialRepository()

  def obtenerPin(tokenHash: String): Future[Validation[PersistenceException, Option[PinAgente]]] =
    repo obtenerPin tokenHash map {
      case zSuccess(pinOpt: Option[PinAgente]) =>
        pinOpt match {
          case Some(pin) => zSuccess(Some(pin))
          case None => zSuccess(None)
        }
      case zFailure(error) => zFailure(error)
    }

  def eliminarPin(tokenHash: String): Future[Validation[PersistenceException, Int]] = repo eliminarPin tokenHash

}
