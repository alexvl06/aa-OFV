package portal.transaccional.fiduciaria.autenticacion.storage.daos.daos.driver

import scala.concurrent.Future

/**
 * Created by alexandra on 25/07/16.
 */
trait GenericDAO[+DTO] {
  def consultar(id: Int): Future[Option[String]]
  //def crear (id: Int, dispositivo: String): Future[Int]
  def eliminar(id: List[Int]): Future[Seq[Int]]
  //def actualizar (id: Int): Future[Int]
}
