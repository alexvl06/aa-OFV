package portal.transaccional.fiduciaria.autenticacion.storage.daos.portal

import co.com.alianza.persistence.entities.PermisoAgenteInmobiliario

import scala.concurrent.Future

/**
 * Created by alexandra on 2016
 */
trait PermisoInmobiliarioDAOs {

  def create(permisos: Seq[PermisoAgenteInmobiliario]): Future[Option[Int]]

  def updateByProject(pEliminados: Seq[PermisoAgenteInmobiliario], pAgregados: Seq[PermisoAgenteInmobiliario]): Future[Option[Int]]

  def delete(permisos: Seq[PermisoAgenteInmobiliario]): Future[Unit]

  def findByIdAgente(idAgente: Int): Future[Seq[PermisoAgenteInmobiliario]]

  def findByProyecto(proyecto: Int): Future[Seq[PermisoAgenteInmobiliario]]

  def findByFid(proyecto: Int): Future[Seq[PermisoAgenteInmobiliario]]

  def findBy3(proyecto: Int, idAgente: Int, fid: Int): Future[Seq[PermisoAgenteInmobiliario]]

}
