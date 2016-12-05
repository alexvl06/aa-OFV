package portal.transaccional.autenticacion.service.drivers.recurso

import co.com.alianza.persistence.entities.{ RecursoBackendInmobiliario, RecursoPerfil, RecursoPerfilAgente, RecursoPerfilClienteAdmin }
import portal.transaccional.fiduciaria.autenticacion.storage.daos.portal.{ AlianzaDAO, AlianzaDAOs }

import scala.concurrent.Future

/**
 * Created by s4n on 16
 */
case class RecursoDriverRepository(generalDAO: AlianzaDAOs) extends RecursoRepository {

  def obtenerRecursos(usuarioId: Int): Future[Seq[RecursoPerfil]] = generalDAO.getResources(usuarioId)

  def obtenerRecursosClienteIndividual(): Future[Seq[RecursoPerfil]] = generalDAO.getClienteIndividualResources()

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
   * PinEmpresa
   * Filtra el listado de recursos que concuerden con la url
   * @param recurso recursos asociados al admin
   * @param url la url a validar
   * @return
   */
  private def filtrarRecursosClienteAdmin(recurso: RecursoPerfilClienteAdmin, url: String): Boolean = {
    val indexSlash: Int = recurso.urlRecurso.lastIndexOf("/")
    val myUrl: String = (if (recurso.urlRecurso.contains("/*") && url.size > recurso.urlRecurso.size)
      url.substring(0, indexSlash)
    else if (url.endsWith("/")) url.substring(0, url.lastIndexOf("/"))
    else url)
    val urlR: String = recurso.urlRecurso.substring(0, indexSlash)
    urlR.contains(myUrl) && urlR.equals(myUrl) && recurso.acceso
  }

  def filtrarRecurso(recursos: Seq[String], urlI: String): Boolean = {

    val url = urlI.replace("?", "%")

    val encontrarVariablesNumericas = "/:id(\\w*)".r
    val encontrarVariablesAlfaNumericas = "/:\\w*".r
    val encontrarVariablesOpcionales = "%".r

    recursos.exists(
      x => {
        val remplazo1 = "^" + encontrarVariablesNumericas.replaceAllIn(x, "/([0-9]+)") + "$"
        val remplazo2 = encontrarVariablesOpcionales.replaceAllIn(remplazo1, "%(([a-zA-z]+)=([a-zA-Z]*[0-9,]*)(&)?)*")
        val reglaGeneral = encontrarVariablesAlfaNumericas.replaceAllIn(remplazo2, "/([a-zA-Z0-9]*)").r
        url.matches(reglaGeneral.toString())
      }
    )
  }

}
