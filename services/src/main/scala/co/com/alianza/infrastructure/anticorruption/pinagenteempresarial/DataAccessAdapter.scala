package co.com.alianza.infrastructure.anticorruption.pinagenteempresarial

import co.com.alianza.app.MainActors
import co.com.alianza.exceptions.PersistenceException
import co.com.alianza.infrastructure.dto.PinUsuarioAgenteEmpresarial

import co.com.alianza.persistence.entities.PinEmpresa
import co.com.alianza.persistence.repositories.{PinUsuarioAgenteEmpresarialRepository, PinUsuarioEmpresarialAdminRepository}

import scala.concurrent.{Future, ExecutionContext}
import scalaz.{Failure => zFailure, Success => zSuccess}
import scalaz.Validation
import java.sql.Timestamp

/**
 * Created by manuel on 6/01/15.
 */
object DataAccessAdapter {

  implicit val ec: ExecutionContext = MainActors.dataAccesEx
  val repo = new PinUsuarioAgenteEmpresarialRepository()

  def obtenerPin(tokenHash: String): Future[Validation[PersistenceException, Option[PinUsuarioAgenteEmpresarial]]] =
    repo obtenerPin tokenHash map {
      case zSuccess(pinOpt: Option[PinEmpresa]) =>
        pinOpt match {
          case Some(pin) => zSuccess(Some(DataAccessTranslator.pinFromEntityToDto(pin)))
          case None => zSuccess(None)
        }
      case zFailure(error) => zFailure(error)
    }

  def eliminarPin(tokenHash: String): Future[Validation[PersistenceException, Int]] = repo eliminarPin tokenHash

}
