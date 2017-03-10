package portal.transaccional.autenticacion.service.drivers.pregunta

import co.com.alianza.commons.enumerations.TiposCliente._
import co.com.alianza.infrastructure.dto.Respuesta
import co.com.alianza.infrastructure.dto.security.UsuarioAuth
import portal.transaccional.autenticacion.service.web.preguntasAutovalidacion.{ ResponseObtenerPreguntas, ResponseObtenerPreguntasComprobar }

import scala.concurrent.Future

trait PreguntasAutovalidacionRepository {

  def obtenerPreguntas(): Future[ResponseObtenerPreguntas]

  def obtenerPreguntasComprobar(idUsuario: Int, tipoCliente: TiposCliente): Future[ResponseObtenerPreguntasComprobar]

  def validarRespuestas(user: UsuarioAuth, respuestas: List[Respuesta], numeroIntentos: Int): Future[String]

  def bloquearRespuestas(idUsuario: Int, tipoCliente: TiposCliente): Future[Int]

}
