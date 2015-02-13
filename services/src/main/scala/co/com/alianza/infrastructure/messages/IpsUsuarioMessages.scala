package co.com.alianza.infrastructure.messages

import co.com.alianza.commons.enumerations.TiposCliente
import co.com.alianza.commons.enumerations.TiposCliente._
import co.com.alianza.persistence.entities.IpsUsuario
import spray.httpx.SprayJsonSupport
import spray.json._

/**
 *
 * @author smontanez
 */
object IpsUsuarioMessagesJsonSupport extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val AgregarIpsUsuarioMessageFormat = jsonFormat4(AgregarIpsUsuarioMessage)
  implicit val EliminarIpsUsuarioMessageFormat = jsonFormat4(EliminarIpsUsuarioMessage)
}

case class ObtenerIpsUsuarioMessage(idUsuario: Int, nit : String, tipoCliente: TiposCliente) extends MessageService{
}

case class AgregarIpsUsuarioMessage(idUsuario: Option[Int], nit : String, ip: String, tipoCliente: Option[Int]) extends MessageService{
  def toEntityIpsUsuario : IpsUsuario = {
    new IpsUsuario(idUsuario.get, ip)
  }
}

case class EliminarIpsUsuarioMessage(idUsuario: Option[Int], nit : String, ip: String, tipoCliente: Option[Int]) extends MessageService{
  def toEntityIpsUsuario : IpsUsuario = {
    new IpsUsuario(idUsuario.get, ip)
  }
}