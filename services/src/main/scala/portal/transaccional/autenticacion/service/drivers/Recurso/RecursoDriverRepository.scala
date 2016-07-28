package portal.transaccional.autenticacion.service.drivers.Recurso

import co.com.alianza.persistence.entities.RecursoPerfil
import portal.transaccional.fiduciaria.autenticacion.storage.daos.portal.{ AlianzaDAO, AlianzaDAOs }

import scala.concurrent.Future

/**
 * Created by s4n on 16
 */
case class RecursoDriverRepository (generalDAO : AlianzaDAOs) extends RecursoRepository {

  def obtenerRecursos (usuarioId : Int): Future[Seq[RecursoPerfil]] = generalDAO.getResources(usuarioId)

  /**
   * Filtra el listado de recursos que concuerden con la url
   *
   * @param recurso recursos asociados al usuario
   * @param url la url a validar
   * @return
   */
  def filtrarRecursos(recurso: RecursoPerfil, url: String): Boolean = filtrarRecursos(recurso.urlRecurso, recurso.acceso, url)

  def filtrarRecursos(urlRecurso: String, acceso: Boolean, url: String): Boolean = {
    val urlC = urlRecurso.substring(0, urlRecurso.lastIndexOf("*"))
    if (urlRecurso.equals(url) || (urlRecurso.endsWith("/*") && urlC.equals(url + "/"))) {
      acceso
    } else if (urlRecurso.endsWith("/*") && url.length >= urlC.length) {
      url.substring(0, urlC.length).equals(urlC) && acceso
    } else { false }
  }

}
