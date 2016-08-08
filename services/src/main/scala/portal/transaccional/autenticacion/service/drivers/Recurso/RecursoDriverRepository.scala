package portal.transaccional.autenticacion.service.drivers.Recurso

import co.com.alianza.persistence.entities.{ RecursoPerfil, RecursoPerfilAgente, RecursoPerfilClienteAdmin }
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
  private def filtrarRecursos(recurso: RecursoPerfil, url: String): Boolean = {
    val indexSlash: Int = recurso.urlRecurso.lastIndexOf("/")
    val myUrl: String = if (recurso.urlRecurso.contains("/*") && url.size > recurso.urlRecurso.size)
      url.substring(0, indexSlash)
    else if (url.endsWith("/")) url.substring(0, url.lastIndexOf("/"))
    else url
    val urlR: String = recurso.urlRecurso.substring(0, indexSlash)
    urlR.contains(myUrl) && urlR.equals(myUrl) && recurso.acceso
  }

  def filtrarRecursosAgente(recursos: Seq[RecursoPerfilAgente], url: String): Seq[RecursoPerfilAgente] = {
    recursos.filter(filtrarRecursosAgente(_, url))
  }

  /**
   * Filtra el listado de recursos que concuerden con la url
   * @param recurso recursos asociados el agente
   * @param url la url a validar
   * @return
   */
  private def filtrarRecursosAgente(recurso: RecursoPerfilAgente, url: String): Boolean = {
    val indexSlash: Int = recurso.urlRecurso.lastIndexOf("/")
    val myUrl: String = if (recurso.urlRecurso.contains("/*") && url.size > recurso.urlRecurso.size)
      url.substring(0, indexSlash)
    else if (url.endsWith("/")) url.substring(0, url.lastIndexOf("/"))
    else url
    val urlR: String = recurso.urlRecurso.substring(0, indexSlash)
    urlR.contains(myUrl) && urlR.equals(myUrl) && recurso.acceso
  }

  def filtrarRecursosClienteAdmin(recursos: Seq[RecursoPerfilClienteAdmin], url: String): Seq[RecursoPerfilClienteAdmin] = {
    recursos.filter(filtrarRecursosClienteAdmin(_, url))
  }

  /**
   * Filtra el listado de recursos que concuerden con la url
   * @param recurso recursos asociados al admin
   * @param url la url a validar
   * @return
   */
  private def filtrarRecursosClienteAdmin(recurso: RecursoPerfilClienteAdmin, url: String): Boolean = {
    val indexSlash: Int = recurso.urlRecurso.lastIndexOf("/")
    val myUrl: String = if (recurso.urlRecurso.contains("/*") && url.size > recurso.urlRecurso.size)
      url.substring(0, indexSlash)
    else if (url.endsWith("/")) url.substring(0, url.lastIndexOf("/"))
    else url
    val urlR: String = recurso.urlRecurso.substring(0, indexSlash)
    urlR.contains(myUrl) && urlR.equals(myUrl) && recurso.acceso
  }

}
