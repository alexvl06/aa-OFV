package portal.transaccional.fiduciaria.autenticacion.storage.daos.portal

import co.com.alianza.persistence.entities.{ PinUsuarioEmpresarialAdmin, RecursoPerfil, UsuarioEmpresarial, UsuarioEmpresarialAdmin }

import scala.concurrent.Future

trait AlianzaDAOs {

  def getResources(idUsuario: Int): Future[Seq[RecursoPerfil]]

  def getAdminTokenAgente(token: String): Future[Option[(UsuarioEmpresarialAdmin, Int)]]

  def getByNitAndUserAgente(nit: String, usuario: String): Future[Option[UsuarioEmpresarial]]

  def getByTokenAgente(token: String): Future[Option[(UsuarioEmpresarial, Int)]]

  def validateAgente(id: String, correo: String, tipoId: Int, idClienteAdmin: Int): Future[Option[UsuarioEmpresarial]]

  def getByNitAndUserAdmin(nit: String, usuario: String): Future[Option[UsuarioEmpresarialAdmin]]

}
