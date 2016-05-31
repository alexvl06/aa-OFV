package co.com.alianza.infrastructure.anticorruption.ultimasContrasenas

import co.com.alianza.app.MainActors
import co.com.alianza.exceptions.PersistenceException
import co.com.alianza.persistence.entities.UltimaContrasena
import co.com.alianza.persistence.repositories.UltimasContrasenasRepository

import scala.concurrent.{ ExecutionContext, Future }
import scalaz.Validation

/**
 * Created by S4N on 14/11/14.
 */
object DataAccessAdapter {

  implicit val ec: ExecutionContext = MainActors.dataAccesEx

  def guardarUltimaContrasena(nuevaUltimaContrasena: UltimaContrasena): Future[Validation[PersistenceException, Unit]] = {
    val repo = new UltimasContrasenasRepository()
    repo.guardarUltimaContrasena(nuevaUltimaContrasena)
  }

  def obtenerUltimasContrasenas(numeroUltimasContrasenas: String, idUsuario: Int): Future[Validation[PersistenceException, List[UltimaContrasena]]] = {
    val repo = new UltimasContrasenasRepository()
    repo.obtenerUltimasContrasenas(numeroUltimasContrasenas, idUsuario)
  }

}
