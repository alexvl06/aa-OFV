package co.com.alianza.infrastructure.anticorruption.pinclienteadmin

import co.com.alianza.app.MainActors
import co.com.alianza.exceptions.PersistenceException
import co.com.alianza.infrastructure.dto.PinUsuarioEmpresarialAdmin

import co.com.alianza.persistence.entities.{PinUsuarioEmpresarialAdmin => ePinUsuarioEmpresarialAdmin}
import co.com.alianza.persistence.repositories.PinUsuarioEmpresarialAdminRepository

import scala.concurrent.{Future, ExecutionContext}
import scalaz.{Failure => zFailure, Success => zSuccess}
import scalaz.Validation
import java.sql.Timestamp

/**
 * Created by manuel on 6/01/15.
 */
object DataAccessAdapter {

  implicit val ec: ExecutionContext = MainActors.dataAccesEx
  val repo = new PinUsuarioEmpresarialAdminRepository()

  def obtenerPin(tokenHash: String): Future[Validation[PersistenceException, Option[PinUsuarioEmpresarialAdmin]]] =
    repo obtenerPin tokenHash map {
      case zSuccess(pinOpt: Option[ePinUsuarioEmpresarialAdmin]) =>
        pinOpt match {
          case Some(pin) => zSuccess(Some(DataAccessTranslator.pinFromEntityToDto(pin)))
          case None => zSuccess(None)
        }
      case zFailure(error) => zFailure(error)
    }

  def eliminarPin(tokenHash: String): Future[Validation[PersistenceException, Int]] = repo eliminarPin tokenHash

}
