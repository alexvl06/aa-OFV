package portal.transaccional.autenticacion.service.drivers.permisoAgenteInmobiliario

import co.com.alianza.commons.enumerations.TiposCliente
import co.com.alianza.commons.enumerations.TiposCliente._
import co.com.alianza.persistence.entities.{ PermisoAgenteInmobiliario, RecursoAgenteInmobiliario }
import portal.transaccional.fiduciaria.autenticacion.storage.daos.portal.{ AlianzaDAO, PermisoInmobiliarioDAOs, RecursoInmobiliarioDAOs }

import scala.concurrent.{ ExecutionContext, Future }

case class PermisoAgenteInmobiliarioDriverRepository(alianzaDao: AlianzaDAO, permisosDAO: PermisoInmobiliarioDAOs, recursoDao : RecursoInmobiliarioDAOs)
  (implicit val ex: ExecutionContext) extends PermisoAgenteInmobiliarioRepository {

  def getPermisosProyecto(identificacion: String, fideicomiso: Int, proyecto: Int): Future[Seq[PermisoAgenteInmobiliario]] = {
    alianzaDao.getPermisosProyectoInmobiliario(identificacion, fideicomiso, proyecto)
  }

  def updatePermisosProyecto(identificacion: String, fideicomiso: Int, proyecto: Int, permisos: Seq[PermisoAgenteInmobiliario]): Future[Option[Int]] = {
    for {
      permisosActuales <- alianzaDao.getPermisosProyectoInmobiliario(identificacion, fideicomiso, proyecto)
      eliminados: Seq[PermisoAgenteInmobiliario] = permisosActuales.diff(permisos)
      agregados: Seq[PermisoAgenteInmobiliario] = permisos.diff(permisosActuales)
      actualizar <- permisosDAO.update(eliminados, agregados)
    } yield actualizar
  }

  def getRecurso(idUser: Int, tiposCliente: TiposCliente): Future[Seq[RecursoAgenteInmobiliario]] = {
    tiposCliente match {
      case TiposCliente.agenteInmobiliario => alianzaDao.getRecursosAgenteInmobiliario(idUser)
      case _ => recursoDao.getAll()
    }
  }

}
