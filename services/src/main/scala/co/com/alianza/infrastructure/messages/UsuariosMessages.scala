package co.com.alianza.infrastructure.messages

import spray.json.DefaultJsonProtocol
import spray.httpx.SprayJsonSupport
import co.com.alianza.persistence.entities.{Usuario => eUsuario}
import java.sql.Timestamp
import java.util.Date
import enumerations.EstadosUsuarioEnum

/**
 *
 * @author smontanez
 */
object UsuariosMessagesJsonSupport extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val UsuarioMessageormat = jsonFormat8(UsuarioMessage)
}


case class UsuarioMessage(correo: String, identificacion: String, tipoIdentificacion: Int, contrasena: String, activarIP:Boolean, clientIp:Option[String] = None, challenge:String = "",uresponse:String="") extends MessageService{

 //TODO: Completar los datos automaticos del usuario
  def toEntityUsuario(claveHash:String):eUsuario = eUsuario(None, correo,new Timestamp(System.currentTimeMillis()),identificacion,  tipoIdentificacion, 1, Some(claveHash), null, 0, None, None)
}

