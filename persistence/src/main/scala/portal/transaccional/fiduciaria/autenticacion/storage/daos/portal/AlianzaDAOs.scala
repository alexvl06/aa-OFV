package portal.transaccional.fiduciaria.autenticacion.storage.daos.portal

import co.com.alianza.persistence.entities._

import scala.concurrent.Future

trait AlianzaDAOs {

  def getClienteIndividualResources(): Future[Seq[RecursoPerfil]]

  def getResources(idUsuario: Int): Future[Seq[RecursoPerfil]]

  def getAgenteResources(idUsuario: Int): Future[Seq[RecursoPerfilAgente]]

  def getAdminResources(idUsuario: Int): Future[Seq[RecursoPerfilClienteAdmin]]

  def getAdminTokenAgente(token: String): Future[Option[(UsuarioEmpresarialAdmin, Int)]]

  def getByNitAndUserAgente(nit: String, usuario: String): Future[Option[UsuarioAgenteEmpresarial]]

  def getByTokenAgente(token: String): Future[(UsuarioAgenteEmpresarial, Int)]

  def validateAgente(id: String, correo: String, tipoId: Int, idClienteAdmin: Int): Future[Option[UsuarioAgenteEmpresarial]]

  def getByNitAndUserAdmin(nit: String, usuario: String): Future[Option[UsuarioEmpresarialAdmin]]

  def getIndividualClientQuestions(idUsuario: Int): Future[Seq[(PreguntaAutovalidacion, RespuestasAutovalidacionUsuario)]]

  def getAdministratorClientQuestions(idUsuario: Int): Future[Seq[(PreguntaAutovalidacion, RespuestasAutovalidacionUsuario)]]

  def deleteIndividualClientAnswers(idUsuario: Int): Future[Int]

  def bloquearRespuestasClienteAdministrador(idUsuario: Int): Future[Int]

}
