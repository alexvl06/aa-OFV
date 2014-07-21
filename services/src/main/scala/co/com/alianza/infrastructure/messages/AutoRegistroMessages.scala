package co.com.alianza.infrastructure.messages

import co.com.alianza.persistence.messages.ConsultaClienteRequest
import spray.json.DefaultJsonProtocol
import spray.httpx.SprayJsonSupport


/**
 *
 * @author smontanez
 */

object ExisteClienteCoreMessagesJsonSupport extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val ExisteClienteCoreMessageFormat = jsonFormat2(ExisteClienteCoreMessage)
}

case class ExisteClienteCoreMessage(tipoDocumento:Int, numDocumento:String)  extends MessageService{
  def toConsultaClienteRequest:ConsultaClienteRequest = ConsultaClienteRequest(tipoDocumento,numDocumento)
}
