package portal.transaccional.autenticacion.service.drivers.usuarioInmobiliario

import co.com.alianza.commons.enumerations.TipoPermisoInmobiliario._
import co.com.alianza.persistence.entities.PermisoAgenteInmobiliario

import scala.concurrent.Future

/**
 * Created by alexandra on 2016
 */
trait UsuarioInmobiliarioRepository {

  def create(proyectos: Seq[Int], agentesInmob: Seq[Int], permisos: Seq[TipoPermisoInmobiliario], fid: Int): Future[Option[Int]]

  def delete(proyectos: Seq[Int], agentesInmob: Seq[Int], permisos: Seq[TipoPermisoInmobiliario], fid: Int): Future[Unit]

  def findByProyect(proyecto : Int): Future[Seq[PermisoAgenteInmobiliario]]

  def updateByPerson(proyectos: Seq[Int], agentesInmob: Seq[Int], permisos: Seq[TipoPermisoInmobiliario], fid: Int): Future[Option[Int]]

  def updateByProject(proyecto: Int, agentesInmob: Seq[Int], permisos: Seq[TipoPermisoInmobiliario], fid: Int): Future[Option[Int]]

  def updateByFid(proyecto: Int, agentesInmob: Seq[(Int,Seq[TipoPermisoInmobiliario])], fid: Int): Future[Option[Int]]

}
