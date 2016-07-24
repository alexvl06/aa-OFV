package co.com.alianza.infrastructure.anticorruption.permisos

import scala.concurrent.{ Future, ExecutionContext }
import scalaz.{ Failure => zFailure, Success => zSuccess }
import scalaz.Validation

import co.com.alianza.app.MainActors
import co.com.alianza.exceptions.PersistenceException
import co.com.alianza.infrastructure.dto._
import co.com.alianza.persistence.repositories.PermisoTransaccionalRepository

/**
 * Created by manuel on 8/01/15.
 */
object PermisoTransaccionalDataAccessAdapter {

  implicit val ec: ExecutionContext = MainActors.dataAccesEx

  val DataAccessTranslator = PermisoTransaccionalDataAccessTranslator

  private[this] def repository = new PermisoTransaccionalRepository

  def guardaPermiso(permiso: PermisoAgente, idsAutorizadores: Option[List[Int]] = None, idClienteAdmin: Int): Future[Validation[PersistenceException, Int]] =
    repository guardarPermiso (DataAccessTranslator aEntity permiso, permiso.seleccionado, idsAutorizadores, idClienteAdmin)

  def guardaPermisoEncargo(permiso: PermisoTransaccionalUsuarioEmpresarial, idsAgentes: Option[List[Int]] = None, idClienteAdmin: Int): Future[Validation[PersistenceException, Int]] =
    repository guardarPermisoEncargo (DataAccessTranslator aEntity permiso, permiso.seleccionado, idsAgentes, idClienteAdmin)

  def consultaPermisosAgente(idAgente: Int): Future[Validation[PersistenceException, (List[Permiso], List[EncargoPermisos])]] = {
    repository.consultaPermisosAgente(idAgente).map {

      case zSuccess( p : (List[(co.com.alianza.persistence.entities.PermisoAgente, List[(Option[co.com.alianza.persistence.entities.PermisoAgenteAutorizador],
        Option[Boolean])])], List[(String, List[(co.com.alianza.persistence.entities.PermisoTransaccionalUsuarioEmpresarial, List[(Option[co.com.alianza
      .persistence.entities.PermisoTransaccionalUsuarioEmpresarialAutorizador], Option[Boolean])])])])) => zSuccess(DataAccessTranslator.aPermisos(p._1, p._2))

      case zFailure(error) => zFailure(error)
    }
  }


  def consultaPermisosAgenteLogin(idAgente: Int): Future[Validation[PersistenceException, Seq[Int]]] = {
    repository.consultaPermisosAgenteLogin(idAgente)
  }

}
