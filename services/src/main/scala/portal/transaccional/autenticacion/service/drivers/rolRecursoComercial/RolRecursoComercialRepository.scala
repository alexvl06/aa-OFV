package portal.transaccional.autenticacion.service.drivers.rolRecursoComercial

import co.com.alianza.persistence.entities.RolComercial

import scala.concurrent.Future

/**
  * Created by dfbaratov on 23/08/16.
  */
trait RolRecursoComercialRepository {

  def obtenerRolesPorRecurso(nombreRecurso:String): Future[Seq[RolComercial]]

}
