package portal.transaccional.autenticacion.service.drivers.permisoAgenteInmobiliario

import co.com.alianza.persistence.entities.PermisoAgenteInmobiliario

import scala.concurrent.Future

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

  /**
    * Actualiza (agrega y/o elimina) los permisos de un proyecto
    *
    * @param identificacion Identificación de la empresa
    * @param fideicomiso    Número identificador del fideicomiso
    * @param proyecto       Número identificador del proyecto
    * @param permisos       Lista de permisos del proyecto
    */
  def updatePermisosProyecto(identificacion: String, fideicomiso: Int, proyecto: Int, permisos: Seq[PermisoAgenteInmobiliario]): Future[Option[Int]]
}
