package portal.transaccional.autenticacion.service.drivers.autorizacion

import co.com.alianza.commons.enumerations.TiposCliente
import co.com.alianza.exceptions.ValidacionException
import co.com.alianza.infrastructure.dto.security.UsuarioAuth
import co.com.alianza.persistence.entities.{ RolComercial, RolRecursoComercial }
import portal.transaccional.autenticacion.service.drivers.rolRecursoComercial.RolRecursoComercialRepository
import portal.transaccional.autenticacion.service.dto.PermisoRecursoDTO

import scala.concurrent.{ ExecutionContext, Future }

/**
 * Created by dfbaratov on 23/08/16.
 */
case class AutorizacionRecursoComercialDriverRepository(rolRecursoRepo: RolRecursoComercialRepository)(implicit val ex: ExecutionContext) extends AutorizacionRecursoComercialRepository {

  override def obtenerRolesPorRecurso(nombreRecurso: String): Future[Seq[RolComercial]] = {
    rolRecursoRepo.obtenerRolesPorRecurso(nombreRecurso)
  }

  override def actualizarRecursos(user: UsuarioAuth, permiso: PermisoRecursoDTO): Future[Option[Int]] = {
    //transformar permisos
    val rolesRecursos: Seq[RolRecursoComercial] = for {
      recurso <- permiso.recursos
      rol <- recurso.roles
    } yield RolRecursoComercial(Some(rol), Some(recurso.idRecurso))
    //actualizar
    for {
      _ <- validarUsuario(user)
      actualizar <- rolRecursoRepo.actualizarPermisos(rolesRecursos)
    } yield actualizar
  }

  private def validarUsuario(user: UsuarioAuth): Future[Boolean] = {
    user.tipoCliente match {
      case TiposCliente.comercialAdmin => Future.successful(true)
      case _ => Future.failed(new ValidacionException("401.1", "Credenciales inválidas"))
    }
  }

}
