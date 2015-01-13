package co.com.alianza.infrastructure.anticorruption.ultimasContrasenasAgenteEmpresarial

/**
 * Created by manuel on 7/01/15.
 */
import co.com.alianza.app.MainActors
import co.com.alianza.exceptions.PersistenceException
import co.com.alianza.persistence.entities.UltimaContrasenaUsuarioAgenteEmpresarial
import co.com.alianza.persistence.repositories.UltimasContrasenasUsuarioAgenteEmpresarialRepository

import scala.concurrent.{ExecutionContext, Future}
import scalaz.Validation

/**
 * Created by S4N on 14/11/14.
 */
object DataAccessAdapter {

  implicit val ec: ExecutionContext = MainActors.dataAccesEx

  def guardarUltimaContrasena(nuevaUltimaContrasena: UltimaContrasenaUsuarioAgenteEmpresarial): Future[Validation[PersistenceException, Unit]] =
    new UltimasContrasenasUsuarioAgenteEmpresarialRepository guardarUltimaContrasena nuevaUltimaContrasena

  def obtenerUltimasContrasenas(numeroUltimasContrasenas: String, idUsuario: Int): Future[Validation[PersistenceException, List[UltimaContrasenaUsuarioAgenteEmpresarial]]] =
    new UltimasContrasenasUsuarioAgenteEmpresarialRepository obtenerUltimasContrasenas (numeroUltimasContrasenas, idUsuario)

}
