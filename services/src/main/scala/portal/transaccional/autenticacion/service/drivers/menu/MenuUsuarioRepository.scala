package portal.transaccional.autenticacion.service.drivers.menu

import scala.collection.mutable.ListBuffer
import scala.concurrent.Future

trait MenuUsuarioRepository {
  /**
   * Obtiene el menú para un perfil determinado
   * @param tokenEncripted Token encriptado
   * @return Future[MenuResponse]
   */
  def getMenu(tokenEncripted: String): Future[MenuResponse]
}
