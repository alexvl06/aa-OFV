package co.com.alianza.infrastructure.messages

import co.com.alianza.commons.enumerations.TiposCliente.TiposCliente
import co.com.alianza.persistence.entities.{ReglasContrasenas, IpsUsuario}
import spray.json.DefaultJsonProtocol
import spray.httpx.SprayJsonSupport
import co.com.alianza.persistence.messages.AgregarIpHabitualRequest

/**
 *
 * @author smontanez
 */
object IpsUsuarioMessagesJsonSupport extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val AgregarIpsUsuarioMessageFormat = jsonFormat2(AgregarIpsUsuarioMessage)
  implicit val EliminarIpsUsuarioMessageFormat = jsonFormat2(EliminarIpsUsuarioMessage)
}


case class ObtenerIpsUsuarioMessage(idUsuario: Int, tipoCliente: TiposCliente) extends MessageService{
}

case class AgregarIpsUsuarioMessage(idUsuario: Option[Int], ip: String) extends MessageService{
  def toEntityIpsUsuario : IpsUsuario = {
    new IpsUsuario(idUsuario.get, ip)
  }
}

case class EliminarIpsUsuarioMessage(idUsuario: Option[Int], ip: String) extends MessageService{
  def toEntityIpsUsuario : IpsUsuario = {
    new IpsUsuario(idUsuario.get, ip)
  }
}