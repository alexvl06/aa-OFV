package portal.transaccional.fiduciaria.autenticacion.storage.daos.portal

import co.com.alianza.commons.enumerations.TiposCliente._
import co.com.alianza.persistence.entities._

import scala.concurrent.Future

trait AlianzaDAOs {

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

  def getPermisosProyectoInmobiliario(nit: String, idFideicomiso: Int, idProyecto: Int, idAgentes: Seq[Int]): Future[Seq[PermisoAgenteInmobiliario]]

  def getPermisosProyectoInmobiliarioByAgente(username: String, idAgente: Int): Future[Seq[PermisoAgenteInmobiliario]]

  def getByTokenAgenteInmobiliario(token: String): Future[UsuarioAgenteInmobiliario]

  //Obtiene el menu de constructor
  def get0(): Future[Seq[RecursoGraficoInmobiliario]]

  //Obtiene el menu de AGENTE
  def get1(username: String, idAgente: Int): Future[Seq[RecursoGraficoInmobiliario]]

  //Obtiene su propio menu
  def get3(idAgente: Int): Future[Seq[RecursoGraficoInmobiliario]]

  //Obtiene los recursos a los que puede acceder Admin
  def get4(): Future[Seq[RecursoBackendInmobiliario]]

  //Obtiene los recursos a los que puede acceder Agente
  def get5(idAgente: Int): Future[Seq[RecursoBackendInmobiliario]]

}
