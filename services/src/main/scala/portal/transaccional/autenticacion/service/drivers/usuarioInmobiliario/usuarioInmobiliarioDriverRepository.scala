package portal.transaccional.autenticacion.service.drivers.usuarioInmobiliario

import portal.transaccional.fiduciaria.autenticacion.storage.daos.portal.PermisoInmobiliarioDAOs

import scala.concurrent.ExecutionContext

/**
 * Created by alexandra on 2016
 */
case class usuarioInmobiliarioDriverRepository (permisos : PermisoInmobiliarioDAOs) (implicit val ex: ExecutionContext) extends usuarioInmobiliarioRepository {

}
