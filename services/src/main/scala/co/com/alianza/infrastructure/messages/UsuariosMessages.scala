package co.com.alianza.infrastructure.messages

import spray.json.DefaultJsonProtocol
import spray.httpx.SprayJsonSupport
import co.com.alianza.persistence.entities.{Usuario => eUsuario}
import java.sql.Timestamp
import enumerations.EstadosUsuarioEnum
import co.com.alianza.persistence.messages.AgregarIpHabitualRequest

/**
 *
 * @author smontanez
 */
object UsuariosMessagesJsonSupport extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val UsuarioMessageormat = jsonFormat10(UsuarioMessage)
  implicit val DesbloquearFormat = jsonFormat10(DesbloquarMessage)
  implicit val DesbloquearWebFormat = jsonFormat7(DesbloquarWebMessage)
  implicit val OlvidoContrasenaMessageFormat = jsonFormat2(OlvidoContrasenaMessage)
}


case class UsuarioMessage(correo: String, identificacion: String, tipoIdentificacion: Int, contrasena: String, activarIP:Boolean, clientIp:Option[String] = None, challenge:String = "",uresponse:String="", primerApellido:Option[String] = None, fechaExpedicion:Option[String] = None) extends MessageService{
 //TODO: Completar los datos automaticos del usuario
  def toEntityUsuario(claveHash:String):eUsuario = eUsuario(None, correo,new Timestamp(System.currentTimeMillis()),identificacion,  tipoIdentificacion,EstadosUsuarioEnum.activo.id, Some(claveHash), null, 0, None, None)
}

case class ConsultaUsuarioMessage(
                                  tipoIdentificacion: Option[Int] = None,
                                  identificacion: Option[String] = None,
                                  correo: Option[String] = None,
                                  token:Option[String] = None
                                   ) extends MessageService

case class ConsultaUsuarioEmpresarialMessage(
                                   tipoIdentificacion: Option[Int] = None,
                                   identificacion: Option[String] = None,
                                   nit: Option[String] = None,
                                   usuario: Option[String] = None,
                                   correo: Option[String] = None,
                                   token:Option[String] = None
                                   ) extends MessageService

case class ConsultaUsuarioEmpresarialAdminMessage(
                                              tipoIdentificacion: Option[Int] = None,
                                              identificacion: Option[String] = None,
                                              nit: Option[String] = None,
                                              usuario: Option[String] = None,
                                              correo: Option[String] = None,
                                              token:Option[String] = None
                                              ) extends MessageService

case class OlvidoContrasenaMessage(identificacion: String, tipoIdentificacion: Int) extends MessageService{

}

case class DesbloquarMessage (correo: String, identificacion: String, tipoIdentificacion: Int, contrasena: String, activarIP:Boolean, clientIp:Option[String] = None, challenge:String = "",uresponse:String="", primerApellido:Option[String] = None, fechaExpedicion:Option[String] = None) extends MessageService{
  def toUsuarioMessage = UsuarioMessage(correo,identificacion,tipoIdentificacion,contrasena,activarIP,clientIp,challenge,uresponse,primerApellido,fechaExpedicion)

}

case class DesbloquarWebMessage ( identificacion: String, tipoIdentificacion: Int, clientIp:Option[String] = None, challenge:String = "",uresponse:String="", primerApellido:Option[String] = None, fechaExpedicion:Option[String] = None) extends MessageService{
  def toDesbloquarMessage = DesbloquarMessage("",identificacion,tipoIdentificacion,"",false,clientIp,challenge,uresponse,primerApellido,fechaExpedicion)

}
