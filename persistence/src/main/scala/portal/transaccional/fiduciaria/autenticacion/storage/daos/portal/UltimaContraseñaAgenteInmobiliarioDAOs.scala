package portal.transaccional.fiduciaria.autenticacion.storage.daos.portal

import co.com.alianza.persistence.entities.UltimaContrasenaAgenteInmobiliario

import scala.concurrent.Future

/**
 * Created by s4n in 2016
 */
trait UltimaContraseñaAgenteInmobiliarioDAOs {

  def create(oldPass: UltimaContrasenaAgenteInmobiliario): Future[Int]

  def findById(passwordValid: Int, idUser: Int): Future[Seq[UltimaContrasenaAgenteInmobiliario]]

}
