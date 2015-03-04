package co.com.alianza.infrastructure.messages

import co.com.alianza.infrastructure.dto._
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
  implicit val AutenticarClienteEmpresaMessageFormat = jsonFormat6(AutenticarUsuarioEmpresarialMessage)
  implicit val AutorizarUrlRequestMessageFormat = jsonFormat2(AutorizarUrl)
  implicit val AgregarIpHabitualRequestMessageFormat = jsonFormat2(AgregarIPHabitualUsuario)
}


case class AutenticarMessage ( tipoIdentificacion:Int, numeroIdentificacion: String, password: String, clientIp: Option[String] = None) extends MessageService {
  def toAutenticarRequest:AutenticacionRequest = AutenticacionRequest(tipoIdentificacion, numeroIdentificacion, password, clientIp)
}

case class AutenticarUsuarioEmpresarialMessage (tipoIdentificacion: Option[Int] = None,
                                                numeroIdentificacion: Option[String] = None,
                                                nit: String,
                                                usuario: String,
                                                password: String,
                                                clientIp: Option[String] = None) extends MessageService {
  //  def toAutenticarRequest:AutenticacionRequest = AutenticacionRequest(tipoIdentificacion, numeroIdentificacion, password, clientIp)
}

case class AutenticarUsuarioEmpresarialAgenteMessage (tipoIdentificacion: Option[Int] = None,
                                                      numeroIdentificacion: Option[String] = None,
                                                      nit: String,
                                                      usuario: String,
                                                      password: String,
                                                      clientIp: Option[String] = None) extends MessageService {
  //  def toAutenticarRequest:AutenticacionRequest = AutenticacionRequest(tipoIdentificacion, numeroIdentificacion, password, clientIp)
}
case class AutenticarUsuarioEmpresarialAdminMessage (tipoIdentificacion: Option[Int] = None,
                                                     numeroIdentificacion: Option[String] = None,
                                                     nit: String,
                                                     usuario: String,
                                                     password: String,
                                                     clientIp: Option[String] = None) extends MessageService {
  //  def toAutenticarRequest:AutenticacionRequest = AutenticacionRequest(tipoIdentificacion, numeroIdentificacion, password, clientIp)
}

case class AutorizarUrl(token:String, url:String)  extends MessageService{
  def toValidarTokenRequest:ValidarTokenRequest = ValidarTokenRequest( token )
}

case class AutorizarUsuarioEmpresarialMessage(token:String, url: Option[String], ip: String)  extends MessageService{
  def toValidarTokenRequest:ValidarTokenRequest = ValidarTokenRequest( token )
}

case class AutorizarUsuarioEmpresarialAdminMessage(token:String, url: Option[String])  extends MessageService{
  def toValidarTokenRequest:ValidarTokenRequest = ValidarTokenRequest( token )
}

case class InvalidarToken(token:String)  extends MessageService {
  def toInvalidarTokenRequest:InvalidarTokenRequest = InvalidarTokenRequest( token )
}

case class AgregarIPHabitualUsuario(idUsuario: Option[Int], clientIp:Option[String] = None)  extends MessageService{
  def toAgregarClienteRequest:AgregarIpHabitualRequest = AgregarIpHabitualRequest(idUsuario.get, clientIp)
}

case class AgregarIPHabitualUsuarioEmpresarialAdmin(idUsuario: Option[Int], clientIp:Option[String] = None)  extends MessageService

case class AgregarIPHabitualUsuarioEmpresarialAgente(idUsuario: Option[Int], clientIp:Option[String] = None)  extends MessageService

//
// Mensajes para el manejo de las sesiones
//

case class ActualizarSesion()

case class CrearSesionUsuario(token: String, tiempoExpiracion: Int, empresa: Option[Empresa] = None)

case class InvalidarSesion(token: String)

case class ExpirarSesion()

case class BuscarSesion(token: String)

case class ObtenerEmpresaSesionActor(token: String)

case class ValidarSesion(token: String)

case class OptenerEmpresaActorPorId(empresaId: Int)


