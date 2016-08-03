package portal.transaccional.fiduciaria.autenticacion.storage.daos.portal

import co.com.alianza.persistence.config.DBConfig
import co.com.alianza.persistence.entities.{PreguntaAutovalidacion, PreguntasAutovalidacionTable}
import slick.lifted.TableQuery

import scala.concurrent.Future

case class PreguntasDAO()(implicit dcConfig: DBConfig) extends TableQuery(new PreguntasAutovalidacionTable(_)) with PreguntasDAOs {

  import dcConfig.db._
  import dcConfig.profile.api._

  def getAll(): Future[Seq[PreguntaAutovalidacion]] = {
    run(this.result)
  }

}
