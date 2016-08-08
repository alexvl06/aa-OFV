package portal.transaccional.fiduciaria.autenticacion.storage.daos.portal

import portal.transaccional.fiduciaria.autenticacion.storage.config.DBConfig
import co.com.alianza.persistence.entities.{ RespuestasAutovalidacionUsuario, RespuestasAutovalidacionUsuarioTable }
import slick.lifted.TableQuery

import scala.concurrent.Future

/**
 * Created by hernando on 25/07/16.
 */
case class RespuestaUsuarioDAO()(implicit dcConfig: DBConfig) extends TableQuery(new RespuestasAutovalidacionUsuarioTable(_)) with RespuestaUsuarioDAOs {

  import dcConfig.DB._
  import dcConfig.driver.api._

  def getById(idUsuario: Int): Future[Seq[RespuestasAutovalidacionUsuario]] = {
    run(this.filter(_.idUsuario === idUsuario).result)
  }

  def delete(idUsuario: Int): Future[Int] = {
    run(this.filter(_.idUsuario === idUsuario).delete)
  }

  def insert(respuestas: List[RespuestasAutovalidacionUsuario]): Future[Option[Int]] = {
    run(this ++= respuestas)
  }

}
