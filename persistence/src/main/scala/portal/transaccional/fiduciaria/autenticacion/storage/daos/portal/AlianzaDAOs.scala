package portal.transaccional.fiduciaria.autenticacion.storage.daos.portal

import co.com.alianza.commons.enumerations.TiposCliente._
import co.com.alianza.persistence.entities.{ Menu, PerfilLdap, ValidacionPerfil, _ }

import scala.concurrent.Future

trait AlianzaDAOs {

  def getClienteIndividualResources(): Future[Seq[RecursoPerfil]]

  def getResources(idUsuario: Int): Future[Seq[RecursoPerfil]]

  def getAgenteResources(idUsuario: Int): Future[Seq[RecursoPerfilAgente]]

  def getAdminResources(idUsuario: Int): Future[Seq[RecursoPerfilClienteAdmin]]

  def getAdminTokenAgente(token: String): Future[Option[(UsuarioEmpresarialAdmin, Int)]]

  def getByNitAndUserAgente(nit: String, usuario: String): Future[Option[UsuarioEmpresarial]]

  def getByTokenAgente(token: String): Future[(UsuarioEmpresarial, Int)]

  def validateAgente(id: String, correo: String, tipoId: Int, idClienteAdmin: Int): Future[Option[UsuarioEmpresarial]]

  def getByNitAndUserAdmin(nit: String, usuario: String): Future[Option[UsuarioEmpresarialAdmin]]

  def getByTokenAdmin(token: String): Future[(UsuarioEmpresarialAdmin, Int)]

  def getIndividualClientQuestions(idUsuario: Int): Future[Seq[(PreguntaAutovalidacion, RespuestasAutovalidacionUsuario)]]

  def getPermisosProyectoInmobiliario(nit: String, idFideicomiso: Int, idProyecto: Int): Future[Seq[PermisoAgenteInmobiliario]]

  def getAdministratorClientQuestions(idUsuario: Int): Future[Seq[(PreguntaAutovalidacion, RespuestasAutovalidacionUsuario)]]

  def deleteIndividualClientAnswers(idUsuario: Int): Future[Int]

  def bloquearRespuestasClienteAdministrador(idUsuario: Int): Future[Int]

  def getPermisosProyectoInmobiliario(nit: String, idFideicomiso: Int, idProyecto: Int, idAgentes: Seq[Int]): Future[Seq[PermisoAgenteInmobiliario]]

  def getResourcesByProjectAndAgent(idAgente: Int, proyecto: Int, fideicomiso: Int): Future[Seq[RecursoBackendInmobiliario]]

  def getPermisosProyectoInmobiliarioByAgente(username: String, idAgente: Int): Future[Seq[PermisoAgenteInmobiliario]]

  def getByTokenAgenteInmobiliario(token: String): Future[UsuarioAgenteInmobiliario]

  def getAdminResourcesVisible(tipoPermisos: String): Future[Seq[RecursoGraficoInmobiliario]]

  def getAgentResourcesById(idAgente: Int): Future[Seq[RecursoGraficoInmobiliario]]

  def getMenuAdmin(isInterno: Boolean): Future[Seq[RecursoBackendInmobiliario]]

  def getBackResourcesByAgent(idAgente: Int): Future[Seq[RecursoBackendInmobiliario]]

  /**OFV LOGIN FASE 1**/
  /**
   * Consulta el menú según el perfil enviado.
   * @param idPerfil Identificador unico del perfil
   * @return Seg[(Menu,Menu)] El primer parametro son las opciones a las que tiene permitido ingresar,
   *         el segundo es todo el menú.
   */
  def getMenuByPerfil(idPerfil: Int): Future[Seq[(Menu, Int, String)]]

  /**
   *
   * @param idPerfil
   * @return
   */
  def getValidacionByPerfil(idPerfil: Int): Future[(Seq[ValidacionPerfil])]

  /**
   * Consulta las opciones del menú a las que tenga permisos el perfil enviado.
   * @param idPerfil Identificador unico del perfil
   * @return Future[(Seq[Menu], Seq[Menu])] El primer parametro son las opciones a las que tiene permitido ingresar,
   *         el segundo es todo el menú.
   */
  def getPermisosMenuByPerfil(idPerfil: Int): Future[Seq[Menu]]

  /**
   * Homologación de perfiles.
   * @param ldapText Validar parametro que llega en el ldap
   * @return
   */
  def getProfileByLdap(ldapText: String): Future[Option[PerfilLdap]]

  /**
   * Realizá consulta si una url esta asociada a un perfil
   * @param idPerfil Identificador del perfil
   * @param url Recurso backend
   * @return Future[Boolean]
   */
  def validResourceByPerfil(idPerfil: Int, url: String): Future[Boolean]
  /**OFV LOGIN FASE 1**/
}
