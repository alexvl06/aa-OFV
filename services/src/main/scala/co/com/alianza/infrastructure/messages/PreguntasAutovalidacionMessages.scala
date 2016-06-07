package co.com.alianza.infrastructure.messages

import co.com.alianza.commons.enumerations.TiposCliente.TiposCliente
import co.com.alianza.infrastructure.dto.{ Pregunta, Respuesta }
import spray.json.DefaultJsonProtocol
import spray.httpx.SprayJsonSupport

/**
 *
 * @author seven4n
 */
object PreguntasAutovalidacionMessagesJsonSupport extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val respuestaMessageFormat = jsonFormat2(Respuesta)
  implicit val respuestasMessageFormat = jsonFormat1(RespuestasMessage)
}

case class ObtenerPreguntasMessage() extends MessageService
case class RespuestasMessage(respuestas: List[Respuesta]) extends MessageService
case class ObtenerPreguntasComprobarMessage(idUsuario: Int, tipoCliente: TiposCliente) extends MessageService
case class BloquearRespuestasMessage(idUsuario: Int, tipoCliente: TiposCliente) extends MessageService
case class GuardarRespuestasMessage(idUsuario: Int, tipoCliente: TiposCliente, respuestas: List[Respuesta]) extends MessageService
case class ValidarRespuestasMessage(idUsuario: Int, tipoCliente: TiposCliente, respuestas: List[Respuesta]) extends MessageService
case class PreguntasResponse(preguntas: List[Pregunta], numeroPreguntas: Int)
