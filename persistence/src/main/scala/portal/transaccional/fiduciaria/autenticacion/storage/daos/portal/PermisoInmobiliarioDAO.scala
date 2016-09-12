package portal.transaccional.fiduciaria.autenticacion.storage.daos.portal

import co.com.alianza.persistence.entities.{ PermisoAgenteInmobiliario, PermisoInmobiliarioTable }
import portal.transaccional.fiduciaria.autenticacion.storage.config.DBConfig
import slick.lifted.TableQuery

import scala.concurrent.Future

/**
 * Created by alexandra on 2016
 */
case class PermisoInmobiliarioDAO()(implicit dcConfig: DBConfig) extends TableQuery(new PermisoInmobiliarioTable(_)) with PermisoInmobiliarioDAOs {

  import dcConfig.DB._
  import dcConfig.driver.api._

  def create(permisos: Seq[PermisoAgenteInmobiliario]): Future[Option[Int]] = {
    run((this ++= permisos).transactionally)
  }

  def updateByPerson(permisos: Seq[PermisoAgenteInmobiliario], idAgente : Int): Future[Option[Int]] = {
    val deleteQuery = this.filter(_.idAgente === idAgente).delete
    val createQuery = this ++= permisos
    run(deleteQuery.andThen(createQuery).transactionally)
  }

  private def find2 (permiso : PermisoAgenteInmobiliario): dcConfig.driver.api.Query[PermisoInmobiliarioTable, PermisoAgenteInmobiliario, Seq] = {
    this.filter(p => p.proyecto === permiso.proyecto && p.idAgente === permiso.idAgente && p.fideicomiso === permiso.fideicomiso &&
                     p.tipoPermiso === permiso.tipoPermiso)
  }

  def delete(permisos: Seq[PermisoAgenteInmobiliario]): Future[Unit] = {
    val queryDelete = permisos.map(p => find2(p).delete)
    val query = DBIO.seq(queryDelete: _*).transactionally
    run(query)
  }

  def findByIdAgente(idAgente: Int): Future[Seq[PermisoAgenteInmobiliario]] = {
    run(this.filter(_.idAgente === idAgente).result)
  }

  def findByProyecto(proyecto: Int): Future[Seq[PermisoAgenteInmobiliario]] = {
    run(this.filter(_.proyecto === proyecto).result)
  }

  def findByFid(fid: Int): Future[Seq[PermisoAgenteInmobiliario]] = {
    run(this.filter(_.fideicomiso === fid).result)
  }

  def findBy3(proyecto: Int, idAgente: Int, fid: Int): Future[Seq[PermisoAgenteInmobiliario]] = {
    run(this.filter(p => p.proyecto === proyecto && p.idAgente === idAgente && p.fideicomiso === fid).result)
  }
}
