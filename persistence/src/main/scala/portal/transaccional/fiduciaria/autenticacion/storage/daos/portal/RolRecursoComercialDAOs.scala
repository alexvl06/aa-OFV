package portal.transaccional.fiduciaria.autenticacion.storage.daos.portal

import java.sql.Timestamp

import co.com.alianza.persistence.entities.{ RolComercial, RolRecursoComercial, UsuarioComercial }

import scala.concurrent.Future

/**
 * Created by s4n on 2016
 */
trait RolRecursoComercialDAOs {

  def obtenerRolesPorRecurso(nombreRecurso: String): Future[Seq[RolComercial]]

  def actualizarPermisos(permisos: Seq[RolRecursoComercial]): Future[Option[Int]]

}
