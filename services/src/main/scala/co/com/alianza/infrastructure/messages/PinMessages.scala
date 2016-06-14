package co.com.alianza.infrastructure.messages

import spray.httpx.SprayJsonSupport
import spray.json.DefaultJsonProtocol

object PinMessages {

  case class ValidarPin(tokenHash: String, funcionalidad: Option[Int]) extends MessageService
  case class CambiarContrasena(tokenHash: String, pw: String, ip: Option[String] = None) extends MessageService
  case class UserPw(pw: String, agregarIp: Option[Boolean] = None)

}

object PinMarshallers extends DefaultJsonProtocol with SprayJsonSupport {
  import co.com.alianza.infrastructure.messages.PinMessages._
  implicit val userPwFormat = jsonFormat2(UserPw)
}
