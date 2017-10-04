package portal.transaccional.fiduciaria.autenticacion.storage.daos.portal

import portal.transaccional.fiduciaria.autenticacion.storage.config.DBConfig
import co.com.alianza.persistence.entities.{ PreguntaAutovalidacion, PreguntasAutovalidacionTable }
import slick.lifted.TableQuery
import scala.concurrent.Future

case class PreguntasDAO()(implicit dcConfig: DBConfig) extends TableQuery(new PreguntasAutovalidacionTable(_)) with PreguntasDAOs {

  import dcConfig.DB._
  import dcConfig.driver.api._

  def getAll(): Future[Seq[PreguntaAutovalidacion]] = {
    run(this.result)
  }

  def getAllActive(): Future[Seq[PreguntaAutovalidacion]] = {
    run(this.filter(_.activa === true).result)
  }

}
