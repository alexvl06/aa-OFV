package co.com.alianza.persistence.messages.empresa

/**
 * Created by s4n on 17/12/14.
 */

case class GetUsuariosEmpresaBusquedaRequest( correo: String, identificacion: String, tipoIdentificacion: Int, estadoUsuario:Int )
