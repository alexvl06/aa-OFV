package portal.transaccional.autenticacion.service.drivers.preguntasAutovalidacion

import portal.transaccional.autenticacion.service.web.preguntasAutovalidacion.PreguntasResponse
import scala.concurrent.Future

trait PreguntasAutovalidacionRepository {

  def obtenerPreguntas(): Future[PreguntasResponse]

}
