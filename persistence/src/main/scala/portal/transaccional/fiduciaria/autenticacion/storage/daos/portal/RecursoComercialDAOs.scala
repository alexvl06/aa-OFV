package portal.transaccional.fiduciaria.autenticacion.storage.daos.portal

import co.com.alianza.persistence.entities.RecursoComercial

import scala.concurrent.Future

/**
 * Created by s4n on 2016
 */
trait RecursoComercialDAOs {

  def getAll(): Future[Seq[RecursoComercial]]

}
