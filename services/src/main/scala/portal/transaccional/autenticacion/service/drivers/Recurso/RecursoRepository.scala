package portal.transaccional.autenticacion.service.drivers.Recurso

import co.com.alianza.persistence.entities.{ RecursoPerfil, RecursoPerfilAgente, RecursoPerfilClienteAdmin }

import scala.concurrent.Future

/**
 * Created by s4n
 */

trait RecursoRepository {

  def obtenerRecursos(usuarioId: Int): Future[Seq[RecursoPerfil]]

  def filtrarRecursos(recursos: Seq[RecursoPerfil], url: String): Seq[RecursoPerfil]

  def filtrarRecursosAgente(recursos: Seq[RecursoPerfilAgente], url: String): Seq[RecursoPerfilAgente]

  def filtrarRecursosClienteAdmin(recursos: Seq[RecursoPerfilClienteAdmin], url: String): Seq[RecursoPerfilClienteAdmin]

}
