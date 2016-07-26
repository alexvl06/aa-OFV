package portal.transaccional.autenticacion.service.drivers.usuario

import co.com.alianza.persistence.entities.UsuarioEmpresarial

import scala.concurrent.Future

/**
 * Created by hernando on 26/07/16.
 */
trait UsuarioEmpresarialRepository {

  def getByIdentificacion(numeroIdentificacion: String): Future[Option[UsuarioEmpresarial]]

}
