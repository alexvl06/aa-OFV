package portal.transaccional.autenticacion.service.drivers.usuarioInmobiliario

import scala.concurrent.ExecutionContext

/**
 * Created by alexandra on 2016
 */
case class UsuarioInmobiliarioDriverRepository()(implicit val ex: ExecutionContext) extends UsuarioInmobiliarioRepository { }
