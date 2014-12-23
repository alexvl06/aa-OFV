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

case class ReiniciarContrasenaAgenteEMessage(numIdentificacionAgenteEmpresarial: String, correoUsuarioAgenteEmpresarial: String, tipoIdentiAgenteEmpresarial: Int) extends MessageService

object AdministrarContrasenaEmpresaMessagesJsonSupport extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val ReiniciarContrasenaEmpresaMessageFormat = jsonFormat3(ReiniciarContrasenaAgenteEMessage)
}

case class UsuarioMessageCorreo(correo: String, identificacion: String, tipoIdentificacion: Int) extends MessageService{
  def toEntityUsuario( estado: EstadosUsuarioEnum.estadoUsuario): eUsuario = eUsuario(None, correo,new Timestamp(System.currentTimeMillis()),identificacion,  tipoIdentificacion, estado.id, None, None, 0, None, None)
}
