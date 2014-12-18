package co.com.alianza.infrastructure.messages.empresa

import spray.json.DefaultJsonProtocol
import spray.httpx.SprayJsonSupport
import co.com.alianza.persistence.entities.{Usuario => eUsuario, UsuarioEmpresarialAdmin => eUsuarioEmpresarialAdmin}
import co.com.alianza.persistence.entities.{Usuario => eUsuario}
import co.com.alianza.persistence.entities.{IpUsuario => eIpUsuario}
import java.sql.Timestamp
import co.com.alianza.constants.EstadosUsuarioEnum
import co.com.alianza.persistence.messages.GetUsuariosBusquedaRequest
import co.com.alianza.persistence.messages.empresa.GetUsuariosBusquedaRequest

/**
 *
 * @author smontanez
 */
object UsuariosEmpresaMessagesJsonSupport extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val GetUsuariosEmpresaBusquedaRequestMessageFormat = jsonFormat4(GetUsuariosEmpresaBusquedaMessage)
}

object HelperUsuario{

  def darEstadoUsuario( estadoUsuario:String ) = {
    var estadoUsuarioEntero: Int = -1
    if( estadoUsuario == null || estadoUsuario.isEmpty )
      estadoUsuarioEntero = -1
    else
      estadoUsuarioEntero = estadoUsuario.toInt
    estadoUsuarioEntero
  }

  def darTipoIdentificacion( tipoIdentificacion:String ) : Int =  {
    var tipoIdentificacionEntero: Int = -1
    if( tipoIdentificacion == null || tipoIdentificacion.isEmpty )
      tipoIdentificacionEntero = -1
    else
      tipoIdentificacionEntero = tipoIdentificacion.toInt
    tipoIdentificacionEntero
  }
}

case class GetUsuariosEmpresaBusquedaMessage(correo: String, identificacion: String, tipoIdentificacion: String, estadoUsuario:String) extends MessageService{
  def toGetUsuariosEmpresaBusquedaRequest:GetUsuariosBusquedaRequest = GetUsuariosBusquedaRequest( correo, identificacion, HelperUsuario.darTipoIdentificacion( tipoIdentificacion ), HelperUsuario.darEstadoUsuario( estadoUsuario ) )
}