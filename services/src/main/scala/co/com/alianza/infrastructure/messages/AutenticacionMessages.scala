package co.com.alianza.infrastructure.messages

import co.com.alianza.infrastructure.dto.Configuracion
import co.com.alianza.persistence.messages.{InvalidarTokenRequest, AgregarIpHabitualRequest, AutenticacionRequest, ValidarTokenRequest}

import spray.httpx.SprayJsonSupport
import spray.json.DefaultJsonProtocol
import akka.actor.ActorRef

/**
 *
 * @author seven4n
 */

object AutenticacionMessagesJsonSupport extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val AutenticarRequestMessageFormat = jsonFormat4(AutenticarMessage)
  implicit val AutenticarClienteEmpresaMessageFormat = jsonFormat6(AutenticarUsuarioEmpresaMessage)
  implicit val AutorizarUrlRequestMessageFormat = jsonFormat2(AutorizarUrl)
  implicit val AgregarIpHabitualRequestMessageFormat = jsonFormat3(AgregarIPHabitualUsuario)
}


case class AutenticarMessage ( tipoIdentificacion:Int, numeroIdentificacion: String, password: String, clientIp: Option[String] = None) extends MessageService {
  def toAutenticarRequest:AutenticacionRequest = AutenticacionRequest(tipoIdentificacion, numeroIdentificacion, password, clientIp)
}

case class AutenticarUsuarioEmpresaMessage (tipoIdentificacion: Option[Int] = None, numeroIdentificacion: Option[String] = None, nit: String, usuario: String, password: String, clientIp: Option[String] = None) extends MessageService {
//  def toAutenticarRequest:AutenticacionRequest = AutenticacionRequest(tipoIdentificacion, numeroIdentificacion, password, clientIp)
}


case class AutorizarUrl(token:String, url:String)  extends MessageService{
  def toValidarTokenRequest:ValidarTokenRequest = ValidarTokenRequest( token )
}

case class AutorizarUsuarioEmpresarialUrl(token:String, url:String, sender: ActorRef)  extends MessageService{
  def toValidarTokenRequest:ValidarTokenRequest = ValidarTokenRequest( token )
}

case class InvalidarToken(token:String)  extends MessageService {
  def toInvalidarTokenRequest:InvalidarTokenRequest = InvalidarTokenRequest( token )
}

case class AgregarIPHabitualUsuario(tipoIdentificacion:Int, numeroIdentificacion: String, clientIp:Option[String] = None)  extends MessageService {
  def toAgregarClienteRequest:AgregarIpHabitualRequest = AgregarIpHabitualRequest(tipoIdentificacion, numeroIdentificacion, clientIp)
}

//
// Mensajes para el manejo de las sesiones
//

case class ActualizarSesion()

case class CrearSesionUsuario(token: String, tiempoExpiracion: Option[Configuracion])

case class InvalidarSesion(token: String)

case class ExpirarSesion()

case class ValidarSesion(token: String)


