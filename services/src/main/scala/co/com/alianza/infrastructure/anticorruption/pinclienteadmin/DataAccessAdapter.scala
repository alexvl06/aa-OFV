package co.com.alianza.infrastructure.anticorruption.pinclienteadmin

import co.com.alianza.exceptions.PersistenceException
import co.com.alianza.persistence.entities.{ PinAdmin => ePinUsuarioEmpresarialAdmin }
import co.com.alianza.persistence.repositories.PinUsuarioEmpresarialAdminRepository

import scala.concurrent.{ ExecutionContext, Future }
import scalaz.{ Failure => zFailure, Success => zSuccess }
import scalaz.Validation

import co.com.alianza.persistence.util.DataBaseExecutionContext

/**
 * Created by manuel on 6/01/15.
 */
object DataAccessAdapter {

  implicit val ec: ExecutionContext = DataBaseExecutionContext.executionContext

  val repo = new PinUsuarioEmpresarialAdminRepository()

  def obtenerPin(tokenHash: String): Future[Validation[PersistenceException, Option[ePinUsuarioEmpresarialAdmin]]] =
    repo obtenerPin tokenHash map {
      case zSuccess(pinOpt: Option[ePinUsuarioEmpresarialAdmin]) =>
        pinOpt match {
          case Some(pin) => zSuccess(Some(pin))
          case None => zSuccess(None)
        }
      case zFailure(error) => zFailure(error)
    }

  def eliminarPin(tokenHash: String): Future[Validation[PersistenceException, Int]] = repo eliminarPin tokenHash

}
