package co.com.alianza.infrastructure.anticorruption.pin

import co.com.alianza.exceptions.PersistenceException
import co.com.alianza.infrastructure.dto.PinUsuario
import co.com.alianza.persistence.entities.{PinUsuario => ePinUsuario}
import co.com.alianza.persistence.repositories.PinRepository
import co.com.alianza.persistence.util.DataBaseExecutionContext

import scala.concurrent.{ExecutionContext, Future}
import scalaz.{Validation, Failure => zFailure, Success => zSuccess}

object DataAccessAdapter {

  implicit val ec: ExecutionContext = DataBaseExecutionContext.executionContext

  val repo = new PinRepository()

  def obtenerPin(tokenHash: String): Future[Validation[PersistenceException, Option[PinUsuario]]] = {
    repo.obtenerPin(tokenHash).map {
      case zSuccess(pinOpt: Option[ePinUsuario]) =>
        pinOpt match {
          case Some(pin) => zSuccess(Some(DataAccessTranslator.pinFromEntityToDto(pin)))
          case None => zSuccess(None)
        }
      case zFailure(error) => zFailure(error)
    }
  }

  def eliminarPin(tokenHash: String): Future[Validation[PersistenceException, Int]] = {
    repo.eliminarPin(tokenHash)
  }

}