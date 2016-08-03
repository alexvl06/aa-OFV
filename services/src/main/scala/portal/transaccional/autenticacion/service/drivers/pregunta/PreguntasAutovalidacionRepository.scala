package portal.transaccional.autenticacion.service.drivers.pregunta

import portal.transaccional.autenticacion.service.web.preguntasAutovalidacion.Response

import scala.concurrent.Future

trait PreguntasAutovalidacionRepository {

  def obtenerPreguntas(): Future[Response]

}
