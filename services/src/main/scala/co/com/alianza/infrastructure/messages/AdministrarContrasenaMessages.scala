package co.com.alianza.infrastructure.messages

import spray.json.DefaultJsonProtocol
import spray.httpx.SprayJsonSupport

/**
 * Created by seven4n on 28/08/14.
 */
object AdministrarContrasenaMessagesJsonSupport  extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val CambiarContrasenaMessageFormat = jsonFormat3(CambiarContrasenaMessage)
}

case class CambiarContrasenaMessage(pw_actual: String, pw_nuevo: String, idUsuario: Option[Int]) extends MessageService
