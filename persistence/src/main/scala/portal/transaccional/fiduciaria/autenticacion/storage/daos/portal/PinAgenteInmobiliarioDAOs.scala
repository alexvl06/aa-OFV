package portal.transaccional.fiduciaria.autenticacion.storage.daos.portal

import co.com.alianza.persistence.entities.PinAgenteInmobiliario

import scala.concurrent.Future

/**
 * Definición de las operaciones de persistencia sobre la entidad PinAgenteInmobiliario
 */
trait PinAgenteInmobiliarioDAOs {

  /**
   * Inserta un pin generado a la tabla
   *
   * @param pin Pin a insertar
   * @return Un futuro con el identificador del pin creado
   */
  def create(pin: PinAgenteInmobiliario): Future[Option[Int]]

  /**
   * Obtiene un pin generado para un agente inmobiliario dado su hash
   *
   * @param tokenHash Hash del pin a buscar
   */
  def get(tokenHash: String): Future[Option[PinAgenteInmobiliario]]

  /**
   * Elimina un pin asociado a un agente inmobiliario
   *
   * @param tokenHash Hash del pin a eliminar
   * @return Un futuro con el número de filas modificadas (debería ser siempre 1)
   */
  def delete(tokenHash: String): Future[Int]
}
