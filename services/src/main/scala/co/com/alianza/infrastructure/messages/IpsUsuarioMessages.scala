package co.com.alianza.infrastructure.messages

import co.com.alianza.persistence.entities.{ReglasContrasenas, IpsUsuario}
import spray.json.DefaultJsonProtocol
import spray.httpx.SprayJsonSupport

/**
 *
 * @author smontanez
 */
object IpsUsuarioMessagesJsonSupport extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val AgregarIpsUsuarioMessageFormat = jsonFormat2(AgregarIpsUsuarioMessage)
  implicit val EliminarIpsUsuarioMessageFormat = jsonFormat2(EliminarIpsUsuarioMessage)
}


case class ObtenerIpsUsuarioMessage(idUsuario: Int) extends MessageService{

}

case class AgregarIpsUsuarioMessage(idUsuario: Int, ip: String) extends MessageService{
  def toEntityIpsUsuario : IpsUsuario = {
    new IpsUsuario(idUsuario, ip)
  }
}

case class EliminarIpsUsuarioMessage(idUsuario: Int, ip: String) extends MessageService{
  def toEntityIpsUsuario : IpsUsuario = {
    new IpsUsuario(idUsuario, ip)
  }
}