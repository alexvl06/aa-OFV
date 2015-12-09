package co.com.alianza.infrastructure.messages

import spray.json.DefaultJsonProtocol
import spray.httpx.SprayJsonSupport

/**
 *
 * @author seven4n
 */
object PreguntasConfrontacionMessagesJsonSupport extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val respuestaMessageFormat = jsonFormat2(Respuesta)
  implicit val guardarRespuestasMessageFormat = jsonFormat3(GuardarRespuestasMessage)
}


case class ObtenerPreguntasMessage() extends MessageService
case class GuardarRespuestasMessage(idUsuario: Int, tipoCliente: String, respuestasList: List[Respuesta]) extends MessageService
case class Respuesta(idPregunta: Int, respuesta: String)
