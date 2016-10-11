package portal.transaccional.autenticacion.service.drivers.pregunta

import co.com.alianza.commons.enumerations.TiposCliente._
import co.com.alianza.infrastructure.dto.Respuesta
import portal.transaccional.autenticacion.service.web.preguntasAutovalidacion.{ ResponseObtenerPreguntas, ResponseObtenerPreguntasComprobar }

import scala.concurrent.Future

trait PreguntasAutovalidacionRepository {

  def obtenerPreguntas(): Future[ResponseObtenerPreguntas]

  def obtenerPreguntasComprobar(idUsuario: Int, tipoCliente: TiposCliente): Future[ResponseObtenerPreguntasComprobar]

  def validarRespuestas(idUsuario: Int, tipoCliente: TiposCliente, respuestas: List[Respuesta], numeroIntentos: Int): Future[String]

  def bloquearRespuestas(idUsuario: Int, tipoCliente: TiposCliente): Future[Int]

}
