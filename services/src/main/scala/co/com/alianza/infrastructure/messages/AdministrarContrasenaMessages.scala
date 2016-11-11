package co.com.alianza.infrastructure.messages

import co.com.alianza.exceptions.{ ValidacionException, ValidacionExceptionPasswordRules }
import spray.json.DefaultJsonProtocol
import spray.httpx.SprayJsonSupport

case class CambiarContrasenaMessage(pw_actual: String, pw_nuevo: String, idUsuario: Option[Int]) extends MessageService

case class CambiarContrasenaCaducadaRequestMessage(token: String, pw_actual: String, pw_nuevo: String) extends MessageService

case class CambiarContrasenaCaducadaMessage(token: String, pw_actual: String, pw_nuevo: String, us_id: Int, us_tipo: String) extends MessageService

object AdministrarContrasenaMessagesJsonSupport extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val CambiarContrasenaMessageFormat = jsonFormat3(CambiarContrasenaMessage)
  implicit val CambiarContrasenaCaducadaMessageFormat = jsonFormat3(CambiarContrasenaCaducadaRequestMessage)
  implicit val ConflictoContraseña = jsonFormat5(ValidacionExceptionPasswordRules)
  implicit val ContrasenaNoValida = jsonFormat2(ValidacionException)
}