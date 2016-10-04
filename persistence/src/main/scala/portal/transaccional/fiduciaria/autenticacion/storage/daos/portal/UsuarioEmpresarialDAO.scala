package portal.transaccional.fiduciaria.autenticacion.storage.daos.portal

import co.com.alianza.persistence.entities.{ UsuarioEmpresarial, UsuarioEmpresarialTable }
import portal.transaccional.fiduciaria.autenticacion.storage.config.DBConfig
import slick.lifted.TableQuery

import scala.concurrent.Future

/**
 * Created by s4n on 2016
 */
case class UsuarioEmpresarialDAO(implicit dcConfig: DBConfig) extends UsuarioAgenteDAO[UsuarioEmpresarialTable, UsuarioEmpresarial](
  TableQuery[UsuarioEmpresarialTable]
) with UsuarioEmpresarialDAOs {

  val n = TableQuery[UsuarioEmpresarialTable]

  import dcConfig.DB._
  import dcConfig.driver.api._

  def create(agenteEmpresarial: UsuarioEmpresarial): Future[Int] = {
    run((n returning n.map(_.id)) += agenteEmpresarial)
  }

  override def update(id: Int, usuario: String, correo: String, nombreUsuario: String, cargo: String, descripcion: String): Future[Int] = {
    val query = n.filter(_.id === id).map(a => (a.correo, a.usuario, a.nombreUsuario, a.cargo, a.descripcion))
    run(query.update(correo, usuario, nombreUsuario, cargo, Some(descripcion)))
  }
}

