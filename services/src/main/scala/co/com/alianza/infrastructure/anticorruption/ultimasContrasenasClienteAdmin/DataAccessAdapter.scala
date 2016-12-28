package co.com.alianza.infrastructure.anticorruption.ultimasContrasenasClienteAdmin

/**
 * Created by manuel on 7/01/15.
 */

import co.com.alianza.exceptions.PersistenceException
import co.com.alianza.persistence.entities.UltimaContrasena
import co.com.alianza.persistence.repositories.UltimasContrasenasUsuarioEmpresarialAdminRepository
import co.com.alianza.persistence.util.DataBaseExecutionContext

import scala.concurrent.{ ExecutionContext, Future }
import scalaz.Validation

/**
 * Created by S4N on 14/11/14.
 */
object DataAccessAdapter {

  implicit val ec: ExecutionContext = DataBaseExecutionContext.executionContext

  def guardarUltimaContrasena(nuevaUltimaContrasena: UltimaContrasena): Future[Validation[PersistenceException, Int]] =
    new UltimasContrasenasUsuarioEmpresarialAdminRepository guardarUltimaContrasena nuevaUltimaContrasena

  def obtenerUltimasContrasenas(numeroUltimasContrasenas: String, idUsuario: Int): Future[Validation[PersistenceException, Seq[UltimaContrasena]]] =
    new UltimasContrasenasUsuarioEmpresarialAdminRepository obtenerUltimasContrasenas (numeroUltimasContrasenas, idUsuario)

}
