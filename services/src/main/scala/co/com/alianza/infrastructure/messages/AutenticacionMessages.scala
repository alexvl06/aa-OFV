package co.com.alianza.infrastructure.messages

import spray.json.DefaultJsonProtocol
import spray.httpx.SprayJsonSupport
import co.com.alianza.persistence.messages.{AgregarIpHabitualRequest, AutenticacionRequest, ValidarTokenRequest}

/**
 *
 * @author seven4n
 */

object AutenticacionMessagesJsonSupport extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val AutenticarRequestMessageFormat = jsonFormat4(AutenticarMessage)
  implicit val AgregarIpHabitualRequestMessageFormat = jsonFormat3(AgregarIPHabitualUsuario)
  implicit val AutorizarUrlRequestMessageFormat = jsonFormat2(AutorizarUrl)
}


case class AutenticarMessage ( tipoIdentificacion:Int, numeroIdentificacion: String, password: String, clientIp:Option[String] = None) extends MessageService{
  def toAutenticarRequest:AutenticacionRequest = AutenticacionRequest(tipoIdentificacion, numeroIdentificacion, password, clientIp)
}


case class AgregarIPHabitualUsuario(tipoIdentificacion:Int, numeroIdentificacion: String, clientIp:Option[String] = None)  extends MessageService{
  def toAgregarClienteRequest:AgregarIpHabitualRequest = AgregarIpHabitualRequest(tipoIdentificacion, numeroIdentificacion, clientIp)
}

case class AutorizarUrl(token:String, url:String)  extends MessageService{
  def toValidarTokenRequest:ValidarTokenRequest = ValidarTokenRequest( token )
}

