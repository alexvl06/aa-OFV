package co.com.alianza.infrastructure.messages

import spray.json.DefaultJsonProtocol
import spray.httpx.SprayJsonSupport

/**
 *
 * @author seven4n
 */
object PreguntasAutovalidacionMessagesJsonSupport extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val respuestaMessageFormat = jsonFormat2(Respuesta)
  implicit val guardarRespuestasMessageFormat = jsonFormat3(GuardarRespuestasMessage)
  implicit val validarRespuestasMessageFormat = jsonFormat3(ValidarRespuestasMessage)
}


case class ObtenerPreguntasMessage() extends MessageService
case class ObtenerPreguntasRandomMessage(idUsuario: Option[Int], tipoCliente: Option[String]) extends MessageService
case class GuardarRespuestasMessage(idUsuario: Option[Int], tipoCliente: Option[String], respuestas: List[Respuesta]) extends MessageService
case class ValidarRespuestasMessage(idUsuario: Option[Int], tipoCliente: Option[String], respuestas: List[Respuesta]) extends MessageService
case class Respuesta(idPregunta: Int, respuesta: String)


