package co.com.alianza.infrastructure.anticorruption.permisos

import scala.concurrent.{Future, ExecutionContext}
import scalaz.{Failure => zFailure, Success => zSuccess}
import scalaz.Validation

import co.com.alianza.app.MainActors
import co.com.alianza.exceptions.PersistenceException
import co.com.alianza.infrastructure.dto._
import co.com.alianza.persistence.repositories.PermisoTransaccionalRepository
import co.com.alianza.persistence.entities.{
  PermisoTransaccionalUsuarioEmpresarial => ePermisoTransaccionalUsuarioEmpresarial,
  PermisoTransaccionalUsuarioEmpresarialAutorizador => ePermisoTransaccionalUsuarioEmpresarialAutorizador
}

/**
 * Created by manuel on 8/01/15.
 */
object PermisoTransaccionalDataAccessAdapter {

  implicit val ec: ExecutionContext = MainActors.dataAccesEx

  val DataAccessTranslator = PermisoTransaccionalDataAccessTranslator

  private[this] def repository = new PermisoTransaccionalRepository

  def guardaPermiso (permiso: PermisoTransaccionalUsuarioEmpresarial, idsAgentes: Option[List[Int]] = None) : Future[Validation[PersistenceException, Int]] =
     repository guardarPermiso ( DataAccessTranslator aEntity permiso, permiso.seleccionado,  idsAgentes)

  def consultaPermisosAgente (idAgente: Int) : Future[Validation[PersistenceException, List[EncargoPermisos]]] =
    repository consultaPermisosAgente idAgente map {
      case zSuccess(listaPermisos: List[(String, List[(ePermisoTransaccionalUsuarioEmpresarial, List[Option[ePermisoTransaccionalUsuarioEmpresarialAutorizador]])])]) =>
        zSuccess(listaPermisos map { e => DataAccessTranslator aEncargoPermisosDTO(e._1, e._2) })
      case zFailure(error) => zFailure(error)
    }

}
