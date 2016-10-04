package portal.transaccional.fiduciaria.autenticacion.storage.daos.portal

import co.com.alianza.persistence.entities.PinAgenteInmobiliario

import scala.concurrent.Future

/**
  * Definición de las operaciones de persistencia sobre la entidad PinAgenteInmobiliario
  */
trait PinAgenteInmobiliarioDAOs {

  /**
    * Inserta un pin generado a la tabla
    * @param pin Pin a insertar
    * @return Un futuro con el identificador del pin creado
    */
  def create(pin: PinAgenteInmobiliario): Future[Option[Int]]
}
