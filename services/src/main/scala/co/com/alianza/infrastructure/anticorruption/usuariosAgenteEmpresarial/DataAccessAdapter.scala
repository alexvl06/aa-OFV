package co.com.alianza.infrastructure.anticorruption.usuariosAgenteEmpresarial

import co.com.alianza.app.MainActors
import co.com.alianza.exceptions.PersistenceException
import co.com.alianza.persistence.repositories.UsuariosEmpresarialRepository

import scala.concurrent.{ExecutionContext, Future}
import scalaz.Validation

/**
 * Created by S4N on 16/12/14.
 */
object DataAccessAdapter {

  implicit val ec: ExecutionContext = MainActors.dataAccesEx

  def validacionAgenteEmpresarial( numIdentificacionAgenteEmpresarial: String, correoUsuarioAgenteEmpresarial: String, tipoIdentiAgenteEmpresarial: Int): Future[Validation[PersistenceException, Option[Int]]] = {
    val repo = new UsuariosEmpresarialRepository()
    repo.validacionAgenteEmpresarial( numIdentificacionAgenteEmpresarial: String, correoUsuarioAgenteEmpresarial: String, tipoIdentiAgenteEmpresarial: Int )
  }

}
