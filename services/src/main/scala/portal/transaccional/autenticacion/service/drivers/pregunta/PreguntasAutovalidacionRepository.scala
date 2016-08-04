package portal.transaccional.autenticacion.service.drivers.pregunta

import co.com.alianza.commons.enumerations.TiposCliente._
import portal.transaccional.autenticacion.service.web.preguntasAutovalidacion.{ ResponseObtenerPreguntas, ResponseObtenerPreguntasComprobar }

import scala.concurrent.Future

trait PreguntasAutovalidacionRepository {

  def obtenerPreguntas(): Future[ResponseObtenerPreguntas]

  def obtenerPreguntasComprobar(idUsuario: Int, tipoCliente: TiposCliente): Future[ResponseObtenerPreguntasComprobar]

}
