package co.com.alianza.infrastructure.anticorruption.contrasenas

import co.com.alianza.exceptions.PersistenceException
import co.com.alianza.persistence.entities.ReglaContrasena
import co.com.alianza.persistence.repositories.ReglasContrasenasRepository
import co.com.alianza.persistence.util.DataBaseExecutionContext
import co.com.alianza.util.clave.Crypto

import scala.concurrent.{ ExecutionContext, Future }
import scalaz.{ Validation, Failure => zFailure, Success => zSuccess }

/**
 * Created by david on 16/06/14.
 */
object DataAccessAdapter {

  implicit val ec: ExecutionContext = DataBaseExecutionContext.executionContext

  val repo = new ReglasContrasenasRepository()

  def consultarReglasContrasenas(): Future[Validation[PersistenceException, Seq[ReglaContrasena]]] = {
    repo.obtenerReglas()
  }

  def obtenerRegla(llave: String): Future[Validation[PersistenceException, Option[ReglaContrasena]]] = {
    repo.obtenerRegla(llave)
  }

  def actualizarContrasena(pw_nuevo: String, idUsuario: Int): Future[Validation[PersistenceException, Int]] = {
    repo.actualizarContrasena(Crypto.hashSha512(pw_nuevo, idUsuario), idUsuario)
  }

}
