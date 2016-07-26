package portal.transaccional.fiduciaria.autenticacion.storage.daos.portal

import co.com.alianza.persistence.config.DBConfig
import co.com.alianza.persistence.entities.{ UsuarioEmpresarialTable, UsuarioEmpresarial }
import slick.lifted.TableQuery

import scala.concurrent.Future

/**
 * Created by hernando on 26/07/16.
 */
case class UsuarioEmpresarialDAO(implicit dcConfig: DBConfig) extends TableQuery(new UsuarioEmpresarialTable(_)) with UsuarioEmpresarialDAOs {

  import dcConfig.db._
  import dcConfig.profile.api._

  def getByIdentity(numeroIdentificacion: String): Future[Option[UsuarioEmpresarial]] = {
    run(this.filter(_.identificacion === numeroIdentificacion).result.headOption)
  }

}
