package portal.transaccional.autenticacion.service.drivers.rolRecursoComercial

import co.com.alianza.persistence.entities.{RolComercial, RolRecursoComercial}
import portal.transaccional.fiduciaria.autenticacion.storage.daos.portal.RolRecursoComercialDAOs

import scala.concurrent.{ExecutionContext, Future}

/**
 * Created by dfbaratov on 23/08/16.
 */
case class RolRecursoComercialDriverRepository(rolRecursoComercialDAO: RolRecursoComercialDAOs)(implicit val ex: ExecutionContext) extends RolRecursoComercialRepository {

  override def obtenerRolesPorRecurso(nombreRecurso: String): Future[Seq[RolComercial]] = rolRecursoComercialDAO.obtenerRolesPorRecurso(nombreRecurso)

  override def actualizarPermisos(permisos: Seq[RolRecursoComercial]) : Future[Option[Int]] = rolRecursoComercialDAO.actualizarPermisos(permisos)
}
