package portal.transaccional.autenticacion.service.drivers.rolRecursoComercial

import co.com.alianza.persistence.entities.{ RecursoComercial, RolComercial }
import portal.transaccional.fiduciaria.autenticacion.storage.daos.portal.{ RecursoComercialDAOs, RolRecursoComercialDAOs }

import scala.concurrent.{ ExecutionContext, Future }

/**
 * Created by dfbaratov on 23/08/16.
 */
case class RecursoComercialDriverRepository(recursoComercialDAO: RecursoComercialDAOs, rolRecursoComercialDAO: RolRecursoComercialDAOs)(implicit val ex: ExecutionContext) extends RecursoComercialRepository {

  override def obtenerTodosConRoles(): Future[Seq[(RecursoComercial, Seq[RolComercial])]] = {

    for {
      persistenceRecursos <- recursoComercialDAO.getAll()
      listaRolesF = persistenceRecursos.map(recurso => rolRecursoComercialDAO.obtenerRolesPorRecurso(recurso.nombre))
      listaRoles <- Future.sequence(listaRolesF)
      listaRecursosRoles = persistenceRecursos zip listaRoles
    } yield listaRecursosRoles
  }

}
