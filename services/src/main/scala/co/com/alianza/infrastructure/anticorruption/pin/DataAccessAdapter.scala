package co.com.alianza.infrastructure.anticorruption.pin

import co.com.alianza.app.MainActors
import co.com.alianza.exceptions.PersistenceException
import co.com.alianza.infrastructure.dto.PinUsuario

import co.com.alianza.persistence.entities.{PinUsuario => ePinUsuario}
import co.com.alianza.persistence.repositories.PinRepository

import scala.concurrent.{Future, ExecutionContext}
import scalaz.{Failure => zFailure, Success => zSuccess}
import scalaz.Validation

object DataAccessAdapter {

  implicit val ec: ExecutionContext = MainActors.dataAccesEx
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

}
