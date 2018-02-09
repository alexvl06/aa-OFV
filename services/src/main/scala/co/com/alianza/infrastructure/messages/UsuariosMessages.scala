package co.com.alianza.infrastructure.messages

import spray.json.DefaultJsonProtocol
import spray.httpx.SprayJsonSupport
import co.com.alianza.persistence.entities.{ Usuario => eUsuario }
import java.sql.Timestamp
import enumerations.EstadosUsuarioEnum
import co.com.alianza.persistence.messages.AgregarIpHabitualRequest

/**
 *
 * @author smontanez
 */
object UsuariosMessagesJsonSupport extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val UsuarioMessageormat = jsonFormat10(UsuarioMessage)
  implicit val DesbloquearFormat = jsonFormat1(DesbloquearMessage)
  implicit val OlvidoContrasenaMessageFormat = jsonFormat4(OlvidoContrasenaMessage)
  implicit val UsuarioHabeasDataFormat = jsonFormat7(UsuarioAceptaHabeasDataMessage)
}

case class UsuarioMessage(correo: String, identificacion: String, tipoIdentificacion: Int, contrasena: String, activarIP: Boolean, clientIp: Option[String] = None, challenge: String = "", uresponse: String = "", primerApellido: Option[String] = None, fechaExpedicion: Option[String] = None) extends MessageService {
  //TODO: Completar los datos automaticos del usuario
  def toEntityUsuario: eUsuario = eUsuario(None, correo, new Timestamp(System.currentTimeMillis()), identificacion, tipoIdentificacion, EstadosUsuarioEnum.activo.id, None, null, 0, None, None)
}

case class ConsultaUsuarioMessage(
  tipoIdentificacion: Option[Int] = None,
  identificacion: Option[String] = None,
  correo: Option[String] = None,
  token: Option[String] = None
) extends MessageService

case class ConsultaUsuarioEmpresarialMessage(
  tipoIdentificacion: Option[Int] = None,
  identificacion: Option[String] = None,
  nit: Option[String] = None,
  usuario: Option[String] = None,
  correo: Option[String] = None,
  token: Option[String] = None
) extends MessageService

case class ConsultaUsuarioEmpresarialAdminMessage(
  tipoIdentificacion: Option[Int] = None,
  identificacion: Option[String] = None,
  nit: Option[String] = None,
  usuario: Option[String] = None,
  correo: Option[String] = None,
  token: Option[String] = None
) extends MessageService

case class UsuarioAceptaHabeasDataMessage(
  perfilCliente: Int,
  identificacion: String,
  tipoIdentificacion: Int,
  correoCliente: String,
  idUsuario: Option[Int],
  habeasData: Boolean,
  idFormulario: String
) extends MessageService

case class OlvidoContrasenaMessage(perfilCliente: Int, identificacion: String, tipoIdentificacion: Int, usuarioClienteAdmin: Option[String]) extends MessageService

case class DesbloquearMessage(identificacion: String) extends MessageService
