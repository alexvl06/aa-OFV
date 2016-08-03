package portal.transaccional.autenticacion.service.drivers.pregunta

import co.com.alianza.persistence.entities.PreguntaAutovalidacion
import portal.transaccional.fiduciaria.autenticacion.storage.daos.portal.PreguntasDAOs

import scala.concurrent.{ ExecutionContext, Future }

case class PreguntasDriverRepository(preguntaDAO: PreguntasDAOs)(implicit val ex: ExecutionContext) extends PreguntasRepository {

  def obtenerPreguntas(): Future[Seq[PreguntaAutovalidacion]] = {
    preguntaDAO.getAll()
  }
}
