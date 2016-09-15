package portal.transaccional.autenticacion.service.drivers.usuarioAgenteInmobiliario

import co.com.alianza.persistence.entities.{ UsuarioAgenteInmobiliario, UsuarioEmpresarial }
import portal.transaccional.fiduciaria.autenticacion.storage.daos.portal.UsuarioAgenteInmobDAOs

import scala.concurrent.{ ExecutionContext, Future }

/**
 * Created by alexandra on 2016
 */
class UsuarioAgenteInmobiliarioDriverRepository (usuarioDAO: UsuarioAgenteInmobDAOs)(implicit val ex: ExecutionContext) extends
  UsuarioAgenteInmobiliarioRepository  {

  def getByIdentityAndUser(identificacion: String, usuario: String): Future[Option[UsuarioAgenteInmobiliario]] = {
    usuarioDAO.getByIdentityAndUser(identificacion, usuario)
  }

  def actualizarIngresosErroneosUsuario(idUsuario: Int, numeroIntentos: Int): Future[Int] = usuarioDAO.updateByIncorrectEntries(idUsuario, numeroIntentos)

}
