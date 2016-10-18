package portal.transaccional.autenticacion.service.drivers.permisoAgenteInmobiliario

import co.com.alianza.commons.enumerations.TiposCliente.TiposCliente
import co.com.alianza.persistence.entities.{ PermisoAgenteInmobiliario, RecursoAgenteInmobiliario }

import scala.concurrent.Future

trait PermisoAgenteInmobiliarioRepository {

  /**
   * Obtiene la relación de los permisos gráficos de los asesores de una empresa asociados a un proyecto
   *
   * @param identificacion Identificación de la empresa
   * @param fideicomiso    Número identificador del fideicomiso
   * @param proyecto       Número identificador del proyecto
   * @param idAgentes      Identificadores de los agentes a retornar los permisos
   * @return La lista de permisos delproyecto
   */
  def getPermisosProyecto(identificacion: String, fideicomiso: Int, proyecto: Int, idAgentes: Seq[Int]): Future[Seq[PermisoAgenteInmobiliario]]

  /**
   * Actualiza (agrega y/o elimina) los permisos de un proyecto
   *
   * @param idAgente Id de la persona que fue autenticada
   * @param username    Número identificador del fideicomiso
   */
  def getPermisosProyectoByAgente(idAgente : Int , username : String): Future[Seq[PermisoAgenteInmobiliario]]

  /**
   * Actualiza (agrega y/o elimina) los permisos de un proyecto
   *
   * @param identificacion Identificación de la empresa
   * @param fideicomiso    Número identificador del fideicomiso
   * @param proyecto       Número identificador del proyecto
   * @param permisos       Lista de permisos del proyecto
   */
  def updatePermisosProyecto(identificacion: String, fideicomiso: Int, proyecto: Int, permisos: Seq[PermisoAgenteInmobiliario]): Future[Option[Int]]

  /**
   * Actualiza (agrega y/o elimina) los permisos de un proyecto
   *
   * @param idUser Id de la persona que fue autenticada, puede ser tanto constructor como agente inmobiliario.
   * @param tiposCliente Tipo de constructor o agenteInmobiliario [co.com.alianza.commons.enumerations.TiposCliente]
   */
  def getRecurso(idUser: Int, tiposCliente: TiposCliente): Future[Seq[RecursoAgenteInmobiliario]]


}
