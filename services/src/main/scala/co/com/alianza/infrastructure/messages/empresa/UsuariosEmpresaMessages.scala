package co.com.alianza.infrastructure.messages.empresa

import spray.json.DefaultJsonProtocol
import spray.httpx.SprayJsonSupport
import co.com.alianza.persistence.messages.empresa.GetUsuariosEmpresaBusquedaRequest
import co.com.alianza.infrastructure.messages.MessageService

/**
 *
 * @author smontanez
 */
object UsuariosEmpresaMessagesJsonSupport extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val GetUsuariosEmpresaBusquedaRequestMessageFormat = jsonFormat5(GetUsuariosEmpresaBusquedaMessage)
}

case class GetUsuariosEmpresaBusquedaMessage(correo: String, usuario: String, nombre: String, estado:Int, idClienteAdmin:Int) extends MessageService{
  def toGetUsuariosEmpresaBusquedaRequest:GetUsuariosEmpresaBusquedaRequest = GetUsuariosEmpresaBusquedaRequest( correo, usuario, nombre, estado, idClienteAdmin )
}