package portal.transaccional.fiduciaria.autenticacion.storage.daos.portal

import co.com.alianza.persistence.entities.{ PreguntaAutovalidacion }

import scala.concurrent.Future

trait PreguntasDAOs {

  def getAll(): Future[Seq[PreguntaAutovalidacion]]

  def getAllActive(): Future[Seq[PreguntaAutovalidacion]]

}
