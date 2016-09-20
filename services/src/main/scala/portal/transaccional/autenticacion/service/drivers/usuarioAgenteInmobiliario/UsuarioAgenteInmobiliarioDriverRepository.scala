package portal.transaccional.autenticacion.service.drivers.usuarioAgenteInmobiliario

import co.com.alianza.persistence.entities.{ UsuarioAgenteInmobiliario, UsuarioAgenteInmobiliarioTable }
import portal.transaccional.fiduciaria.autenticacion.storage.daos.portal.UsuarioAgenteDAOs

import scala.concurrent.ExecutionContext

/**
 * Created by alexandra on 2016
 */
class UsuarioAgenteInmobiliarioDriverRepository (usuarioDAO: UsuarioAgenteDAOs[UsuarioAgenteInmobiliarioTable,UsuarioAgenteInmobiliario])(implicit val ex:
ExecutionContext) extends UsuarioAgenteInmobiliarioRepository  {}
