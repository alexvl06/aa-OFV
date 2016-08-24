package portal.transaccional.autenticacion.service.drivers.autorizacion

import co.com.alianza.persistence.entities.RolComercial
import portal.transaccional.autenticacion.service.drivers.rolRecursoComercial.RolRecursoComercialRepository

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by dfbaratov on 23/08/16.
  */
case class AutorizacionRecursoComercialDriverRepository(rolRecursoRepo: RolRecursoComercialRepository)(implicit val ex: ExecutionContext) extends AutorizacionRecursoComercialRepository{

  override def obtenerRolesPorRecurso(nombreRecurso: String): Future[Seq[RolComercial]] = {
    rolRecursoRepo.obtenerRolesPorRecurso(nombreRecurso);
  }
}
