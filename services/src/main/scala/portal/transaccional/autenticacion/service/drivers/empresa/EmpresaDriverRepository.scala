package portal.transaccional.autenticacion.service.drivers.empresa

import co.com.alianza.persistence.entities.Empresa
import portal.transaccional.fiduciaria.autenticacion.storage.daos.portal.EmpresaDAOs

import scala.concurrent.{ ExecutionContext, Future }

/**
 * Created by hernando on 26/07/16.
 */
case class EmpresaDriverRepository(empresaDAO: EmpresaDAOs)(implicit val ex: ExecutionContext) extends EmpresaRepository {

  def getByIdentity(nit: String): Future[Option[Empresa]] = {
    empresaDAO.getByIdentity(nit)
  }

}
