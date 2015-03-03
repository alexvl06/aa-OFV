package co.com.alianza.infrastructure.messages

import spray.httpx.SprayJsonSupport
import spray.json.DefaultJsonProtocol

object PinMessages {

  case class ValidarPin(tokenHash: String, funcionalidad: Option[Int]) extends MessageService
  case class CambiarPw(tokenHash: String, pw: String) extends MessageService
  case class UserPw(pw: String)

}

object PinMarshallers extends DefaultJsonProtocol with SprayJsonSupport {
  import co.com.alianza.infrastructure.messages.PinMessages._
  implicit val userPwFormat = jsonFormat1(UserPw)
}
