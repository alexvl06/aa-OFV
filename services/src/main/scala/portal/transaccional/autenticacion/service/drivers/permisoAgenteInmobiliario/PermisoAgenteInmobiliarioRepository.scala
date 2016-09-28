package portal.transaccional.autenticacion.service.drivers.permisoAgenteInmobiliario

import co.com.alianza.commons.enumerations.TipoPermisoInmobiliario._
import co.com.alianza.persistence.entities.PermisoAgenteInmobiliario

import scala.concurrent.Future

/**
  * Created by alexandra on 15/09/16.
  */
trait PermisoAgenteInmobiliarioRepository {

  /**
    * Obtiene la relación de los permisos gráficos de los asesores de una empresa asociados a un proyecto
    *
    * @param identificacion Identificación de la empresa
    * @param fideicomiso    Número identificador del fideicomiso
    * @param proyecto       Número identificador del proyecto
    * @return La lista de permisos delproyecto
    */
  def getPermisosProyecto(identificacion: String, fideicomiso: Int, proyecto: Int): Future[Seq[PermisoAgenteInmobiliario]]

  def create(proyectos: Seq[Int], agentesInmob: Seq[Int], permisos: Seq[TipoPermisoInmobiliario], fid: Int): Future[Option[Int]]

  def delete(proyectos: Seq[Int], agentesInmob: Seq[Int], permisos: Seq[TipoPermisoInmobiliario], fid: Int): Future[Unit]

  def findByProyect(proyecto: Int): Future[Seq[PermisoAgenteInmobiliario]]

  def updateByProject(proyecto: Int, agentesInmob: Seq[Int], permisos: Seq[TipoPermisoInmobiliario], fid: Int): Future[Option[Int]]

  def updateByFid(proyecto: Int, agentesInmob: Seq[(Int, Seq[TipoPermisoInmobiliario])], fid: Int): Future[Option[Int]]

}
