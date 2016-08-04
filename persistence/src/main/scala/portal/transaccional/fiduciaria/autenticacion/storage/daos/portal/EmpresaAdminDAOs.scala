package portal.transaccional.fiduciaria.autenticacion.storage.daos.portal

import scala.concurrent.Future

/**
 * Created by s4n on 2016
 */
trait EmpresaAdminDAOs {

  def obtenerIdEmpresa(idUsuario: Int): Future[Int]

}
