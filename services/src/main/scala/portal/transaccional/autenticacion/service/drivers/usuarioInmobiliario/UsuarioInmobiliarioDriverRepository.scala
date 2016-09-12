package portal.transaccional.autenticacion.service.drivers.usuarioInmobiliario

import co.com.alianza.commons.enumerations.TipoPermisoInmobiliario.TipoPermisoInmobiliario
import co.com.alianza.persistence.entities.PermisoAgenteInmobiliario
import portal.transaccional.fiduciaria.autenticacion.storage.daos.portal.PermisoInmobiliarioDAOs

import scala.concurrent.{ ExecutionContext, Future }

/**
 * Created by alexandra on 2016
 */
case class UsuarioInmobiliarioDriverRepository(permisoDAO: PermisoInmobiliarioDAOs)(implicit val ex: ExecutionContext) extends UsuarioInmobiliarioRepository {

  private def creacionPermiso(proyectos: Seq[Int], agentesInmob: Seq[Int], permisos: Seq[TipoPermisoInmobiliario], fid: Int): Seq[PermisoAgenteInmobiliario] = {
    proyectos
      .flatMap(proyecto => agentesInmob
        .flatMap(agente => permisos
          .map(permiso => PermisoAgenteInmobiliario(agente, proyecto, fid, permiso.id))))
  }

  /**
   *
   * @param proyectos Lista de identificacion de los proyectos a los cuales se les asignan @permisos
   * @param agentesInmob Lista de Ids de los agentes inmobiliarios (Salas de  ventas) a los cuales se les asignan los permisos
   * @param permisos Lista de permisos de tipo [[co.com.alianza.commons.enumerations.TipoPermisoInmobiliario]], que se otorgaran a los agentesInmob
   * @param fid Numero de fideicomiso a la cual pertenecen los proyectos
   */
  def create(proyectos: Seq[Int], agentesInmob: Seq[Int], permisos: Seq[TipoPermisoInmobiliario], fid: Int): Future[Option[Int]] = {
    val permisosInmobiliarios = creacionPermiso(proyectos, agentesInmob, permisos, fid)
    permisoDAO.create(permisosInmobiliarios)
  }

  def delete(proyectos: Seq[Int], agentesInmob: Seq[Int], permisos: Seq[TipoPermisoInmobiliario], fid: Int): Future[Unit] = {
    val permisosInmobiliarios = creacionPermiso(proyectos, agentesInmob, permisos, fid)
    permisoDAO.delete(permisosInmobiliarios)
  }

  def findByProyect(proyecto : Int): Future[Seq[PermisoAgenteInmobiliario]] = {
    permisoDAO.findByProyecto(proyecto)
  }

  def update(proyectos: Seq[Int], agentesInmob: Seq[Int], permisos: Seq[TipoPermisoInmobiliario], fid: Int): Future[Option[Int]] = {
    val permisosInmobiliarios = creacionPermiso(proyectos, agentesInmob, permisos, fid)
    permisoDAO.updateByPerson(permisosInmobiliarios, agentesInmob.head)
  }

}
