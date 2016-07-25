package portal.transaccional.autenticacion.service.drivers.usuario

import co.com.alianza.exceptions.ValidacionException
import co.com.alianza.persistence.entities.Usuario
import portal.transaccional.fiduciaria.autenticacion.storage.daos.UsuarioDAOs

import scala.concurrent.{ Future, ExecutionContext }

/**
 * Created by hernando on 25/07/16.
 */
case class UsuarioDriverRepository(usuarioDAO: UsuarioDAOs)(implicit val ex: ExecutionContext) extends UsuarioRepository {

  def getUsuarioByIdentificacion(numeroIdentificacion: String): Future[Usuario] = {
    usuarioDAO.getByIdentity(numeroIdentificacion) flatMap {
      (usuarioOption: Option[Usuario]) =>
        usuarioOption match {
          case Some(usuario: Usuario) => Future.successful(usuario)
          case _ => Future.failed(ValidacionException("401.3", "Error usuario no existe"))
        }
    }
  }

}
