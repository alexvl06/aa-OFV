package co.com.alianza.infrastructure.messages

import spray.json.DefaultJsonProtocol
import spray.httpx.SprayJsonSupport

/**
 *
 * @author smontanez
 */
object IpsUsuarioMessagesJsonSupport extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val IpsUsuarioMessagesFormat = jsonFormat1(ObtenerIpsUsuarioMessage)
}


case class ObtenerIpsUsuarioMessage(idUsuario: Int) extends MessageService{

}

