package portal.transaccional.autenticacion.service.drivers.rolRecursoComercial

import co.com.alianza.persistence.entities.RolComercial
import portal.transaccional.fiduciaria.autenticacion.storage.daos.portal.RolComercialDAOs

import scala.concurrent.{ExecutionContext, Future}

/**
 * Created by dfbaratov on 23/08/16.
 */
case class RolComercialDriverRepository(rolComercialDAO: RolComercialDAOs)(implicit val ex: ExecutionContext) extends RolComercialRepository {

  override def obtenerTodos(): Future[Seq[RolComercial]] = rolComercialDAO.getAll()

}
