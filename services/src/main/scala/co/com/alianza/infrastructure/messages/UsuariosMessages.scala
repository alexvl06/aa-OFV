package co.com.alianza.infrastructure.messages

import spray.json.DefaultJsonProtocol
import spray.httpx.SprayJsonSupport
import co.com.alianza.persistence.entities.{Usuario => eUsuario}
import java.sql.Timestamp
import enumerations.EstadosUsuarioEnum

/**
 *
 * @author smontanez
 */
object UsuariosMessagesJsonSupport extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val UsuarioMessageormat = jsonFormat8(UsuarioMessage)
}


case class UsuarioMessage(correo: String, identificacion: String, tipoIdentificacion: Int, contrasena: String, activarIP:Boolean, clientIp:Option[String] = Some("172.0.0.1"), challenge:String = "",uresponse:String="") extends MessageService{

 //TODO: Completar los datos automaticos del usuario
  def toEntityUsuario(claveHash:String):eUsuario = eUsuario(None, correo,new Timestamp(System.currentTimeMillis()),identificacion,  tipoIdentificacion,EstadosUsuarioEnum.activo.id, Some(claveHash), null, 0, None, None)
}

