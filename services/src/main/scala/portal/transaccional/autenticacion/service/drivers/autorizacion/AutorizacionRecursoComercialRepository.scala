package portal.transaccional.autenticacion.service.drivers.autorizacion

import co.com.alianza.infrastructure.dto.security.UsuarioAuth
import co.com.alianza.persistence.entities.RolComercial
import portal.transaccional.autenticacion.service.dto.PermisoRecursoDTO

import scala.concurrent.Future

/**
 * Created by dfbaratov on 23/08/16.
 */
trait AutorizacionRecursoComercialRepository {

  def obtenerRolesPorRecurso(nombreRecurso: String): Future[Seq[RolComercial]]

  def actualizarRecursos(user: UsuarioAuth, permisos: PermisoRecursoDTO): Future[Option[Int]]

}
