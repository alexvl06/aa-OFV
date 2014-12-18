package co.com.alianza.infrastructure.messages.empresa

import co.com.alianza.infrastructure.messages.MessageService
import spray.json.DefaultJsonProtocol
import spray.httpx.SprayJsonSupport

/**
 * Created by S4N on 17/12/14.
 */

case class ReiniciarContrasenaEmpresaMessage(numIdentificacionAgenteEmpresarial: String, correoUsuarioAgenteEmpresarial: String, tipoIdentiAgenteEmpresarial: Int) extends MessageService

object AdministrarContrasenaEmpresaMessagesJsonSupport extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val ReiniciarContrasenaEmpresaMessageFormat = jsonFormat3(ReiniciarContrasenaEmpresaMessage)
}