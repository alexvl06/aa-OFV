package portal.transaccional.autenticacion.service.drivers.autorizacion

import co.com.alianza.persistence.entities.{RolComercial, RolRecursoComercial}
import portal.transaccional.autenticacion.service.drivers.rolRecursoComercial.RolRecursoComercialRepository
import portal.transaccional.autenticacion.service.dto.PermisoRecursoDTO

import scala.concurrent.{ExecutionContext, Future}

/**
 * Created by dfbaratov on 23/08/16.
 */
case class AutorizacionRecursoComercialDriverRepository(rolRecursoRepo: RolRecursoComercialRepository)(implicit val ex: ExecutionContext) extends AutorizacionRecursoComercialRepository {

  override def obtenerRolesPorRecurso(nombreRecurso: String): Future[Seq[RolComercial]] = {
    rolRecursoRepo.obtenerRolesPorRecurso(nombreRecurso);
  }

  override def actualizarRecursos(permiso: PermisoRecursoDTO) :Future[Option[Int]] = {
    val rolesRecursos: Seq[RolRecursoComercial] = for {
      recurso <- permiso.recursos
      rol <- recurso.roles
    } yield RolRecursoComercial(Some(rol), Some(recurso.idRecurso))
    rolRecursoRepo.actualizarPermisos(rolesRecursos)
  }
}
