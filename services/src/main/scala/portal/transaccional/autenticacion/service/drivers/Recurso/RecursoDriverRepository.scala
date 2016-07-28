package portal.transaccional.autenticacion.service.drivers.Recurso

import co.com.alianza.persistence.entities.RecursoPerfil
import portal.transaccional.fiduciaria.autenticacion.storage.daos.portal.{ AlianzaDAO, AlianzaDAOs }

import scala.concurrent.Future

/**
 * Created by s4n on 16
 */
case class RecursoDriverRepository(generalDAO: AlianzaDAOs) extends RecursoRepository {

  def obtenerRecursos(usuarioId: Int): Future[Seq[RecursoPerfil]] = generalDAO.getResources(usuarioId)

  def filtrarRecursos(recursos: Seq[RecursoPerfil], url: String): Seq[RecursoPerfil] = {
    recursos.filter(filtrarRecursos(_, url))
  }

  /**
   * Filtra el listado de recursos que concuerden con la url
   * @param recurso recursos asociados al usuario
   * @param url la url a validar
   * @return
   */
  def filtrarRecursos(recurso: RecursoPerfil, url: String): Boolean = {
    val urlR = recurso.urlRecurso
    urlR.contains(url) && urlR.substring(0, urlR.lastIndexOf("/")).equals(url) && recurso.acceso
  }

}
