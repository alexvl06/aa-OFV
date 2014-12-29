package co.com.alianza.infrastructure.messages.empresa

import co.com.alianza.infrastructure.messages.MessageService
import spray.json.DefaultJsonProtocol
import spray.httpx.SprayJsonSupport

/**
 * Created by S4N on 17/12/14.
 */

case class CrearAgenteEMessage(
                                 nit: String,
                                 usuario: String,
                                 nombre: String,
                                 apellido: String,
                                 cargo: String,
                                 correo: String,
                                 descripcion: String,
                                 ips: Array[String]
                              ) extends MessageService

object CrearAgenteEMessageJsonSupport extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val CrearAgenteEMessageMessageFormat = jsonFormat8(CrearAgenteEMessage)
}
