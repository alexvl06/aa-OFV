package portal.transaccional.autenticacion.service.drivers.pregunta

import co.com.alianza.persistence.entities.{PreguntaAutovalidacion}

import scala.concurrent.Future

trait PreguntasRepository {

  def obtenerPreguntas(): Future[Seq[PreguntaAutovalidacion]]

}
