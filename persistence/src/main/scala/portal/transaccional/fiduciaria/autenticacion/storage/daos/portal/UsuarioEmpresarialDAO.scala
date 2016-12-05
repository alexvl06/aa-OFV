package portal.transaccional.fiduciaria.autenticacion.storage.daos.portal

import java.sql.Timestamp

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

  import dcConfig.DB._
  import dcConfig.driver.api._

  def create(agenteEmpresarial: UsuarioEmpresarial): Future[Int] = {
    run((table returning table.map(_.id)) += agenteEmpresarial)
  }

  override def update(id: Int, usuario: String, correo: String, nombreUsuario: String, cargo: String, descripcion: String): Future[Int] = {
    val query = table.filter(_.id === id).map(a => (a.correo, a.usuario, a.nombreUsuario, a.cargo, a.descripcion))
    run(query.update(correo, usuario, nombreUsuario, cargo, Some(descripcion)))
  }

  override def updatePasswordById(idUsuario: Int, contrasena: String): Future[Int] = {
    run(table.filter(_.id === idUsuario).map(_.contrasena).update(Option(contrasena)))
  }

  override def updateUpdateDate(idUsuario: Int, fechaActual: Timestamp): Future[Int] = {
    run(table.filter(_.id === idUsuario).map(_.fechaActualizacion).update(fechaActual))
  }

}
