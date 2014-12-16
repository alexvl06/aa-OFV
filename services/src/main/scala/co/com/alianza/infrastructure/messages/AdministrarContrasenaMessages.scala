package co.com.alianza.infrastructure.messages

import spray.json.DefaultJsonProtocol
import spray.httpx.SprayJsonSupport

case class CambiarContrasenaMessage(pw_actual: String, pw_nuevo: String, idUsuario: Option[Int]) extends MessageService

case class CambiarContrasenaCaducadaMessage(token: String, pw_actual: String, pw_nuevo: String) extends MessageService

case class ReiniciarContrasenaMessage(numIdentificacionAgenteEmpresarial: String, correoUsuarioAgenteEmpresarial: String, tipoIdentiAgenteEmpresarial: Int) extends MessageService

object AdministrarContrasenaMessagesJsonSupport extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val CambiarContrasenaMessageFormat = jsonFormat3(CambiarContrasenaMessage)
  implicit val CambiarContrasenaCaducadaMessageFormat = jsonFormat3(CambiarContrasenaCaducadaMessage)
  implicit val ReiniciarContrasenaMessageFormat = jsonFormat3(ReiniciarContrasenaMessage)
}