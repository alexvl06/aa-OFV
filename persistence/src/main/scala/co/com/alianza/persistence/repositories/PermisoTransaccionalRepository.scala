package co.com.alianza.persistence.repositories

import scalaz.Validation
import scala.concurrent.{ExecutionContext, Future}
import co.com.alianza.persistence.entities._
import co.com.alianza.exceptions.PersistenceException
import CustomDriver.simple._
import scala.util.Try

/**
 * Created by manuel on 8/01/15.
 */
class PermisoTransaccionalRepository ( implicit executionContext: ExecutionContext) extends AlianzaRepository {

  val tabla = TableQuery[PermisoTransaccionalUsuarioEmpresarialTable]

  /**
   * Crea o actualiza un permiso
   * @param permiso
   * @return
   */
  def guardarPermiso(permiso: PermisoTransaccionalUsuarioEmpresarial) = loan {
    implicit session =>
      resolveTry(
        Try {
          val q = for {
            p <- tabla if p.idEncargo === permiso.idEncargo && p.idAgente === permiso.idAgente
          } yield (p.montoMaximoTransaccion, p.montoMaximoDiario, p.minimoNumeroPersonas)
          val regMod = q.update((permiso.montoMaximoTransaccion, permiso.montoMaximoDiario, permiso.minimoNumeroPersonas))
          if(regMod==0)
            (tabla returning tabla.map(_=>1)) += permiso
          else
            regMod
        },
        "Guardar permiso transaccional de agente"
      )
  }

}
