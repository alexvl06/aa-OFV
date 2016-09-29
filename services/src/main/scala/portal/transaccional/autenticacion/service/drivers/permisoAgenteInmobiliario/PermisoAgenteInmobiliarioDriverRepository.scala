package portal.transaccional.autenticacion.service.drivers.permisoAgenteInmobiliario

import co.com.alianza.persistence.entities.PermisoAgenteInmobiliario
import portal.transaccional.fiduciaria.autenticacion.storage.daos.portal.{AlianzaDAO, PermisoInmobiliarioDAOs, UsuarioAgenteInmobDAOs}

import scala.concurrent.{ExecutionContext, Future}

case class PermisoAgenteInmobiliarioDriverRepository(alianzaDao: AlianzaDAO,
                                                     usuariosDao: UsuarioAgenteInmobDAOs,
                                                     permisosDAO: PermisoInmobiliarioDAOs)(implicit val ex: ExecutionContext) extends PermisoAgenteInmobiliarioRepository {

  def getPermisosProyecto(identificacion: String, fideicomiso: Int, proyecto: Int): Future[Seq[PermisoAgenteInmobiliario]] = {
    alianzaDao.getPermisosProyectoInmobiliario(identificacion, fideicomiso, proyecto)
  }

  def updatePermisosProyecto(identificacion: String, fideicomiso: Int, proyecto: Int, permisos: Seq[PermisoAgenteInmobiliario]): Future[Option[Int]] = {
    for {
      agentesEmpresa <- usuariosDao.getAll(identificacion)
      permisosFiltrados: Seq[PermisoAgenteInmobiliario] = permisos.filter(permiso => agentesEmpresa.exists(agente => agente.id == permiso.idAgente))
      permisosActuales <- alianzaDao.getPermisosProyectoInmobiliario(identificacion, fideicomiso, proyecto)
      permisosEliminados: Seq[PermisoAgenteInmobiliario] = permisosActuales.diff(permisosFiltrados)
      permisosAgregados: Seq[PermisoAgenteInmobiliario] = permisosFiltrados.diff(permisosActuales)
      actualizar <- permisosDAO.update(permisosEliminados, permisosAgregados)
    } yield actualizar
  }
}
