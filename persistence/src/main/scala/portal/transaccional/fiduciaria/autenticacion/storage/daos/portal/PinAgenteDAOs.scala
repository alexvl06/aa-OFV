package portal.transaccional.fiduciaria.autenticacion.storage.daos.portal

import co.com.alianza.persistence.entities.PinAgente

import scala.concurrent.Future

/**
 * Created by hernando on 26/10/16.
 */
trait PinAgenteDAOs {

  def create(pinAgente: PinAgente): Future[Int]

  def findById(token: String): Future[Option[PinAgente]]

  def delete(token: String): Future[Int]

  def deleteAll(idUsuario: Int): Future[Int]

}
