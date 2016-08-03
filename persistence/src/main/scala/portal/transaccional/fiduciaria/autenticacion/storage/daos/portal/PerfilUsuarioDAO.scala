package portal.transaccional.fiduciaria.autenticacion.storage.daos.daos.driver

import co.com.alianza.persistence.config.DBConfig
import co.com.alianza.persistence.entities.{ PerfilUsuario, PerfilUsuarioTable }
import portal.transaccional.fiduciaria.autenticacion.storage.daos.portal.PerfilUsuarioDAOs
import slick.lifted.TableQuery

import scala.concurrent.Future

/**
 * Created by s4n on 2016
 */
case class PerfilUsuarioDAO()(implicit dcConfig: DBConfig) extends TableQuery(new PerfilUsuarioTable(_)) with PerfilUsuarioDAOs {

  import dcConfig.db._
  import dcConfig.profile.api._

  def create(perfiles: Seq[PerfilUsuario]): Future[Option[Int]] = {
    run(this ++= perfiles)
  }

}
