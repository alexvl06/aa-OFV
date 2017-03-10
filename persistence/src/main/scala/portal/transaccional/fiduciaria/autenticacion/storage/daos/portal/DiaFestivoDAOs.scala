package portal.transaccional.fiduciaria.autenticacion.storage.daos.portal

import java.sql.Date

import co.com.alianza.persistence.entities.DiaFestivo

import scala.concurrent.Future

/**
 * Created by s4n on 2016
 */
trait DiaFestivoDAOs {

  def obtener(fecha: Date): Future[Option[DiaFestivo]]

}
