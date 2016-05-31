package co.com.alianza.infrastructure.messages.empresa

import spray.json.DefaultJsonProtocol
import spray.httpx.SprayJsonSupport
import co.com.alianza.persistence.messages.empresa.GetAgentesEmpresarialesRequest
import co.com.alianza.infrastructure.messages.MessageService

/**
 *
 * @author smontanez
 */
object AgentesEmpresarialesMessagesJsonSupport extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val GetAgentesEmpresarialesRequestMessageFormat = jsonFormat5(GetAgentesEmpresarialesMessage)
}

case class GetAgentesEmpresarialesMessage(correo: String, usuario: String, nombre: String, estado: Int, idClienteAdmin: Int) extends MessageService {
  def toGetUsuariosEmpresaBusquedaRequest: GetAgentesEmpresarialesRequest = GetAgentesEmpresarialesRequest(correo, usuario, nombre, estado, idClienteAdmin)
}
