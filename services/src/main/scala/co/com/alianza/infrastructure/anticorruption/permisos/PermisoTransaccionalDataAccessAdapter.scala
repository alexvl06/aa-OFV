package co.com.alianza.infrastructure.anticorruption.permisos

import scala.concurrent.{Future, ExecutionContext}
import scalaz.Validation

import co.com.alianza.app.MainActors
import co.com.alianza.exceptions.PersistenceException
import co.com.alianza.infrastructure.dto.PermisoTransaccionalUsuarioEmpresarial
import co.com.alianza.persistence.repositories.PermisoTransaccionalRepository

/**
 * Created by manuel on 8/01/15.
 */
object PermisoTransaccionalDataAccessAdapter {

  implicit val ec: ExecutionContext = MainActors.dataAccesEx

  val DataAccessTranslator = PermisoTransaccionalDataAccessTranslator

  def guardaPermiso (permiso: PermisoTransaccionalUsuarioEmpresarial, idsAgentes: Option[List[Int]] = None) : Future[Validation[PersistenceException, Int]] =
    new PermisoTransaccionalRepository guardarPermiso ( DataAccessTranslator aEntity permiso, permiso.seleccionado,  idsAgentes)

}
