package portal.transaccional.fiduciaria.autenticacion.storage.daos.portal

import co.com.alianza.persistence.entities.RecursoGraficoInmobiliario

import scala.concurrent.Future

/**
 * Created by s4n in 2016
 */
trait RecursoInmobiliarioDAOs {

  def getAll(): Future[Seq[RecursoGraficoInmobiliario]]

}
