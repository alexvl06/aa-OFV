package portal.transaccional.fiduciaria.autenticacion.storage.daos.portal

import co.com.alianza.persistence.entities.HorarioEmpresa

import scala.concurrent.Future

/**
 * Created by s4n on 2016
 */
trait HorarioEmpresaDAOs {

  def obtenerHorarioEmpresa(idUsuario: Int): Future[Option[HorarioEmpresa]]

}
