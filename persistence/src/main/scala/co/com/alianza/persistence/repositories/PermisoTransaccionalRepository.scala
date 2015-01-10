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
  val tablaAutorizadores = TableQuery[PermisoTransaccionalUsuarioEmpresarialAutorizadorTable]

  /**
   * Crea o actualiza un permiso
   * @param permiso
   * @return
   */
  def guardarPermiso(permiso: PermisoTransaccionalUsuarioEmpresarial, idsAgentes: Option[List[Int]] = None) = loan {
    implicit session =>
      resolveTry(
        Try {
          val q = for {
            p <- tabla if p.idEncargo === permiso.idEncargo && p.idAgente === permiso.idAgente && p.tipoTransaccion === permiso.tipoTransaccion
          } yield (p.montoMaximoTransaccion, p.montoMaximoDiario, p.minimoNumeroPersonas)
          val regMod = q update ((permiso.montoMaximoTransaccion, permiso.montoMaximoDiario, permiso.minimoNumeroPersonas))
          if(regMod==0){
            tabla += permiso
            1
          } else
            regMod
        },
        "Guardar permiso transaccional de agente"
      )
  }

  private[this] def guardarAgentesPermiso(permiso: PermisoTransaccionalUsuarioEmpresarial, idsAgentes: Option[List[Int]] = None) = {
    if(idsAgentes.isDefined && !idsAgentes.get.isEmpty){
      val ids = idsAgentes.get
      val q = for {
        au <- tablaAutorizadores if au.idEncargo === permiso.idEncargo && au.idAgente === permiso.idAgente && au.tipoTransaccion === permiso.tipoTransaccion
      } yield au
      val ausentes = ids.diff(q.list.map(_.idAutorizador))
      val q2 = for{
        aut <- tablaAutorizadores if aut.idAutorizador in ausentes
      } yield _
      if(regMod==0){
        tabla += permiso
        1
      } else
        regMod
    }
  }

}
