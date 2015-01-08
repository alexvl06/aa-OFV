package co.com.alianza.infrastructure.messages

import spray.json.DefaultJsonProtocol
import spray.httpx.SprayJsonSupport
import co.com.alianza.infrastructure.dto.PermisoTransaccionalUsuarioEmpresarial

/**
 * Created by manuel on 7/01/15.
 */
case class GuardarPermisosAgenteMessage (permisos: List[PermisoTransaccionalUsuarioEmpresarial]) extends MessageService

object PermisosTransaccionalesJsonSupport extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val PermisoTransaccionalUsuarioEmpresarialFormat = jsonFormat6(PermisoTransaccionalUsuarioEmpresarial)
  implicit val GuardarPermisosAgenteFormat = jsonFormat1(GuardarPermisosAgenteMessage)
}