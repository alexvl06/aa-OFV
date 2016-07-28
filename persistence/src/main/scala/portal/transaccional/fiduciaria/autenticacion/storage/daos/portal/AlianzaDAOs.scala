package portal.transaccional.fiduciaria.autenticacion.storage.daos.portal

import co.com.alianza.persistence.entities.{ RecursoPerfil, UsuarioEmpresarialAdmin }

import scala.concurrent.Future

trait AlianzaDAOs {

  def getResources(idUsuario: Int): Future[Seq[RecursoPerfil]]

  def getAdminToken(token: String): Future[Option[(UsuarioEmpresarialAdmin, Int)]]
}
