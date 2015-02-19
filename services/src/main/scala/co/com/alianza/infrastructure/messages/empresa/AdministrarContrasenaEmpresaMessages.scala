package co.com.alianza.infrastructure.messages.empresa

import java.sql.Timestamp

import co.com.alianza.infrastructure.messages.MessageService
import enumerations.EstadosUsuarioEnum
import spray.json.DefaultJsonProtocol
import spray.httpx.SprayJsonSupport
import co.com.alianza.persistence.entities.{Usuario => eUsuario}

/**
 * Created by S4N on 17/12/14.
 */

case class ReiniciarContrasenaAgenteEMessage(numIdentificacionAgenteEmpresarial: String, correoUsuarioAgenteEmpresarial: String, tipoIdentiAgenteEmpresarial: Int, idClienteAdmin: Option[Int]) extends MessageService

case class CambiarContrasenaClienteAdminMessage(pw_actual: String, pw_nuevo: String, idUsuario: Option[Int]) extends MessageService

case class CambiarContrasenaAgenteEmpresarialMessage(pw_actual: String, pw_nuevo: String, idUsuario: Option[Int]) extends MessageService

case class CambiarContrasenaCaducadaClienteAdminMessage(token: String, pw_actual: String, pw_nuevo: String, idUsuario: Option[Int]) extends MessageService

case class CambiarContrasenaCaducadaAgenteEmpresarialMessage(token: String, pw_actual: String, pw_nuevo: String, idUsuario: Option[Int]) extends MessageService

case class AsignarContrasenaMessage(password: String, idUsuario: Int) extends MessageService

object AdministrarContrasenaEmpresaMessagesJsonSupport extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val ReiniciarContrasenaEmpresaMessageFormat = jsonFormat4(ReiniciarContrasenaAgenteEMessage)
  implicit val CambiarContrasenaClienteAdminMessageFormat = jsonFormat3(CambiarContrasenaClienteAdminMessage)
  implicit val CambiarContrasenaAgenteEmpresarialMessageFormat = jsonFormat3(CambiarContrasenaAgenteEmpresarialMessage)
  implicit val CambiarContrasenaCaducadaClienteAdminMessageFormat = jsonFormat4(CambiarContrasenaCaducadaClienteAdminMessage)
  implicit val CambiarContrasenaCaducadaAgenteEmpresarialMessageFormat = jsonFormat4(CambiarContrasenaCaducadaAgenteEmpresarialMessage)
  implicit val AsignarContrasenaMessageFormat = jsonFormat2(AsignarContrasenaMessage)

}

case class UsuarioMessageCorreo(correo: String, identificacion: String, tipoIdentificacion: Int) extends MessageService{
  def toEntityUsuario( estado: EstadosUsuarioEnum.estadoUsuario): eUsuario = eUsuario(None, correo,new Timestamp(System.currentTimeMillis()),identificacion,  tipoIdentificacion, estado.id, None, None, 0, None, None)
}
