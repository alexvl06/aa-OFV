package portal.transaccional.autenticacion.service.drivers.rolRecursoComercial

import co.com.alianza.persistence.entities.{ RecursoComercial, RolComercial }

import scala.concurrent.Future

/**
 * Created by dfbaratov on 23/08/16.
 */
trait RecursoComercialRepository {

  def obtenerTodosConRoles(): Future[Seq[(RecursoComercial, Seq[RolComercial])]]

}
