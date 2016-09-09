package portal.transaccional.fiduciaria.autenticacion.storage.daos.portal

import co.com.alianza.persistence.entities.{ PermisoInmobiliario, PermisoInmobiliarioTable }
import portal.transaccional.fiduciaria.autenticacion.storage.config.DBConfig
import slick.lifted.TableQuery

import scala.annotation.tailrec
import scala.concurrent.Future

/**
 * Created by alexandra on 2016
 */
case class PermisoInmobiliarioDAO () (implicit dcConfig: DBConfig) extends TableQuery(new PermisoInmobiliarioTable(_)) with PermisoInmobiliarioDAOs {

  import dcConfig.DB._
  import dcConfig.driver.api._

  def create(permisos: Seq[PermisoInmobiliario]): Future[Option[Int]] = {
    run((this ++= permisos).transactionally)
  }

  def update(permisos: Seq[PermisoInmobiliario]): Future[Unit] = {
    val query = DBIO.seq(permisos.map( p => this.filter(_ == p).update(p)): _*).transactionally
    run(query)
  }

  def delete(permisos: Seq[PermisoInmobiliario]): Future[Unit] = {
    val query = DBIO.seq(permisos.map( p => this.filter(_ == p).delete): _*).transactionally
    run(query)
  }

  def findByIdAgente(idAgente : Int): Future[Seq[PermisoInmobiliario]] = {
    run(this.filter(_.idAgente == idAgente ).result)
  }

  def findByProyecto(proyecto : Int): Future[Seq[PermisoInmobiliario]] = {
    run(this.filter(_.proyecto == proyecto).result)
  }
}
