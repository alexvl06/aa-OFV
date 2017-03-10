package portal.transaccional.autenticacion.service.drivers.empresa

import co.com.alianza.persistence.entities.Empresa

import scala.concurrent.Future

/**
 * Created by hernando on 26/07/16.
 */
trait EmpresaRepository {

  def getByIdentity(nit: String): Future[Option[Empresa]]

  def validarEmpresa(empresaOption: Option[Empresa]): Future[Boolean]

}
