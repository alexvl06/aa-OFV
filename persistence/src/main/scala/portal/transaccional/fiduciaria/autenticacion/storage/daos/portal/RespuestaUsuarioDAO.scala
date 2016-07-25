package portal.transaccional.fiduciaria.autenticacion.storage.daos.portal

import co.com.alianza.persistence.config.DBConfig
import co.com.alianza.persistence.entities.{ RespuestasAutovalidacionUsuario, RespuestasAutovalidacionUsuarioTable }
import slick.lifted.TableQuery

import scala.concurrent.Future

/**
 * Created by hernando on 25/07/16.
 */
case class RespuestaUsuarioDAO()(implicit dcConfig: DBConfig) extends TableQuery(new RespuestasAutovalidacionUsuarioTable(_)) with RespuestaUsuarioDAOs {

  import dcConfig.db._
  import dcConfig.profile.api._

  def getById(idUsuario: Int): Future[Seq[RespuestasAutovalidacionUsuario]] = {
    run(this.filter(_.idUsuario === idUsuario).result)
  }

}
