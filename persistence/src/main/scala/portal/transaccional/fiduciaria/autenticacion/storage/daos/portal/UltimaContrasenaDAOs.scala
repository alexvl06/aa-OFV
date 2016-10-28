package portal.transaccional.fiduciaria.autenticacion.storage.daos.portal

import co.com.alianza.persistence.entities.UltimaContrasena

import scala.concurrent.Future

/**
 * Created by hernando on 26/10/16.
 */
trait UltimaContrasenaDAOs {

  def create(ultimaContrasena: UltimaContrasena): Future[Int]

  def getByAmount(idUsuario: Int, cantidad: Int): Future[Seq[UltimaContrasena]]

}
