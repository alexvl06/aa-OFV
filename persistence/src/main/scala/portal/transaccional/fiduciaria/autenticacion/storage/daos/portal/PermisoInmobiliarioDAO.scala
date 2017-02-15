package portal.transaccional.fiduciaria.autenticacion.storage.daos.portal

import co.com.alianza.persistence.entities.{ PermisoAgenteInmobiliario, PermisoInmobiliarioTable }
import portal.transaccional.fiduciaria.autenticacion.storage.config.DBConfig
import slick.dbio.Effect.Write
import slick.lifted.TableQuery
import slick.profile.FixedSqlAction

import scala.concurrent.Future

case class PermisoInmobiliarioDAO()(implicit dcConfig: DBConfig) extends TableQuery(new PermisoInmobiliarioTable(_)) with PermisoInmobiliarioDAOs {

  import dcConfig.DB._
  import dcConfig.driver.api.{ DBIOAction, _ }

  def update(eliminados: Seq[PermisoAgenteInmobiliario], agregados: Seq[PermisoAgenteInmobiliario]): Future[Option[Int]] = {
    val delete: DBIOAction[Seq[Int], NoStream, Write] = DBIO.sequence(eliminados.map(deleteAction))
    val insert: FixedSqlAction[Option[Int], NoStream, Write] = this ++= agregados
    run(delete.andThen(insert).transactionally)
  }

  private def deleteAction(permiso: PermisoAgenteInmobiliario): FixedSqlAction[Int, NoStream, Write] = {
    this.filter(p => p.proyecto === permiso.proyecto && p.idAgente === permiso.idAgente
      && p.fideicomiso === permiso.fideicomiso && p.tipoPermiso === permiso.tipoPermiso).delete
  }

  private def insertAction(permisos: Seq[PermisoAgenteInmobiliario]): FixedSqlAction[Option[Int], NoStream, Write] = {
    this ++= permisos
  }
}
