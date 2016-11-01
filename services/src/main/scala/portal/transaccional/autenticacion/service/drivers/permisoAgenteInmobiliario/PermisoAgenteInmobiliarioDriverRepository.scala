package portal.transaccional.autenticacion.service.drivers.permisoAgenteInmobiliario

import co.com.alianza.commons.enumerations.TiposCliente
import co.com.alianza.commons.enumerations.TiposCliente._
import co.com.alianza.persistence.entities.{ PermisoAgenteInmobiliario, RecursoGraficoInmobiliario }
import portal.transaccional.fiduciaria.autenticacion.storage.daos.portal.{ AlianzaDAO, PermisoInmobiliarioDAOs, UsuarioAgenteInmobDAOs }

import scala.concurrent.{ ExecutionContext, Future }

case class PermisoAgenteInmobiliarioDriverRepository(alianzaDao: AlianzaDAO, usuariosDao: UsuarioAgenteInmobDAOs,
  permisosDAO: PermisoInmobiliarioDAOs)(implicit val ex: ExecutionContext) extends PermisoAgenteInmobiliarioRepository {

  def getPermisosProyecto(identificacion: String, fideicomiso: Int, proyecto: Int, idAgentes: Seq[Int]): Future[Seq[PermisoAgenteInmobiliario]] = {
    alianzaDao.getPermisosProyectoInmobiliario(identificacion, fideicomiso, proyecto, idAgentes)
  }

  def getPermisosProyectoByAgente(idAgente : Int , username : String): Future[Seq[PermisoAgenteInmobiliario]] = {
    alianzaDao.getPermisosProyectoInmobiliarioByAgente(username, idAgente)
  }

  def updatePermisosProyecto(identificacion: String, fideicomiso: Int, proyecto: Int,
    idAgentes: Seq[Int], permisos: Seq[PermisoAgenteInmobiliario]): Future[Option[Int]] = {
    for {
      agentesEmpresa <- usuariosDao.getAll(identificacion)
      permisosFiltrados: Seq[PermisoAgenteInmobiliario] = permisos
        .filter(permiso => agentesEmpresa.exists(agente => agente.id == permiso.idAgente) && idAgentes.contains(permiso.idAgente))
      permisosActuales <- alianzaDao.getPermisosProyectoInmobiliario(identificacion, fideicomiso, proyecto, idAgentes)
      permisosEliminados: Seq[PermisoAgenteInmobiliario] = permisosActuales.diff(permisosFiltrados)
      permisosAgregados: Seq[PermisoAgenteInmobiliario] = permisosFiltrados.diff(permisosActuales)
      actualizar <- permisosDAO.update(permisosEliminados, permisosAgregados)
    } yield actualizar
  }

  def getRecurso(idUser: Int, tiposCliente: TiposCliente, isMatriz : Boolean): Future[Seq[RecursoGraficoInmobiliario]] = {

    tiposCliente match {
      case TiposCliente.agenteInmobiliario => alianzaDao.getAgentResourcesById(idUser)
      case _ =>
        if (isMatriz) {
          alianzaDao.getAdminResourcesVisible(false).map(_.filterNot(_.id == 13))
        } else {
          alianzaDao.getAdminResourcesVisible(true)
        }
    }
  }

}
