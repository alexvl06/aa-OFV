package portal.transaccional.fiduciaria.autenticacion.storage.daos.portal

import co.com.alianza.persistence.config.DBConfig
import co.com.alianza.persistence.entities.{ RespuestasAutovalidacionUsuario, RespuestasAutovalidacionUsuarioAdministradorTable }
import slick.lifted.TableQuery

import scala.concurrent.Future

/**
 * Created by hernando on 25/07/16.
 */
case class RespuestaUsuarioAdminDAO()(implicit dcConfig: DBConfig) extends TableQuery(new RespuestasAutovalidacionUsuarioAdministradorTable(_))
    with RespuestaUsuarioDAOs {

  import dcConfig.db._
  import dcConfig.profile.api._

  def getById(idUsuario: Int): Future[Seq[RespuestasAutovalidacionUsuario]] = {
    run(this.filter(_.idUsuario === idUsuario).result)
  }

  override def delete(idUsuario: Int): Future[Int] = ???

  override def insert(respuestas: List[RespuestasAutovalidacionUsuario]): Future[Option[Int]] = ???
}
