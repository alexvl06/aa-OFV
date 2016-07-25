package portal.transaccional.fiduciaria.autenticacion.storage.daos.core

import scala.concurrent.Future

/**
 * Created by hernando on 25/07/16.
 */
trait ClienteDAOs {

  def consultaCliente(numDocumento: String): Future[String]

  def consultaGrupo(idGrupo: Int): Future[String]

}
