package co.com.alianza.infrastructure.messages

object PinMessages {

  case class ValidarPin(tokenHash: String) extends MessageService

}
