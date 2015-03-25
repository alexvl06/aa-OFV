package co.com.alianza.infrastructure.messages.empresa

import co.com.alianza.infrastructure.dto.security.UsuarioAuth
import spray.json.DefaultJsonProtocol
import spray.httpx.SprayJsonSupport

case class RegistrarOTP(deviceid:String, usuario:UsuarioAuth) {
  def toOperacionOTPDTO(application:Int, username:String, account:String) = OperacionOTPDTO(deviceid,application,username,account)
}

case class OperacionOTPDTO(deviceid:String, application:Int, username:String, account:String) {
}

case class RemoverOTP(deviceid:String, usuario:UsuarioAuth) {
  def toOperacionOTPDTO(application:Int, username:String, account:String) = OperacionOTPDTO(deviceid,application,username,account)
}

case class HabilitarOTP(deviceid:String, usuario:UsuarioAuth) {
  def toOperacionOTPDTO(application:Int, username:String, account:String) = OperacionOTPDTO(deviceid,application,username,account)
}

case class DeshabilitarOTP(deviceid:String, usuario:UsuarioAuth) {
  def toOperacionOTPDTO(application:Int, username:String, account:String) = OperacionOTPDTO(deviceid,application,username,account)
}



object OtpMessagesJsonSupport extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val RegistrarOTPDTOMessageFormat = jsonFormat4(OperacionOTPDTO)
}