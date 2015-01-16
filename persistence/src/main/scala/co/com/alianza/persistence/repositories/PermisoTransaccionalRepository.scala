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
   * Crea, actualiza o borra un permiso
   * @param permiso Datos permiso
   * @param estaSeleccionado Se encuentra seleccionado?
   * @param idsAgentes Autorizadores
   * @return
   */
  def guardarPermiso(permiso: PermisoTransaccionalUsuarioEmpresarial, estaSeleccionado: Boolean, idsAgentes: Option[List[Int]] = None) = loan {
    implicit session =>
      resolveTry(
        Try {
          val q = for {
            p <- tabla if p.idEncargo === permiso.idEncargo && p.idAgente === permiso.idAgente && p.tipoTransaccion === permiso.tipoTransaccion
          } yield (p.tipoPermiso, p.montoMaximoTransaccion, p.montoMaximoDiario, p.minimoNumeroPersonas)
          var regMod = 0
          regMod = q update ((permiso.tipoPermiso, permiso.montoMaximoTransaccion, permiso.montoMaximoDiario, permiso.minimoNumeroPersonas))
          if(!estaSeleccionado) {
            guardarAgentesPermiso(permiso, estaSeleccionado, Some(List()))
            regMod = (for {
              p <- tabla if p.idEncargo === permiso.idEncargo && p.idAgente === permiso.idAgente && p.tipoTransaccion === permiso.tipoTransaccion
            } yield p).delete
            regMod
          }
          if(regMod==0 && estaSeleccionado){
            tabla += permiso
            guardarAgentesPermiso(permiso, estaSeleccionado, idsAgentes)
            1
          } else {
            guardarAgentesPermiso(permiso, estaSeleccionado, idsAgentes)
            regMod
          }
        },
        "Guardar permiso transaccional de agente"
      )
  }

  def consultaPermisosAgente(idAgente: Int) = loan {
    implicit session =>
      resolveTry(
        Try {
          val joinPermisosAutorizadores = for {
            (permiso, autorizador) <- tabla.filter(_.idAgente===idAgente) join tablaAutorizadores on {
              (permiso, autorizador) =>
                permiso.idEncargo===autorizador.idEncargo && permiso.idAgente===autorizador.idAgente && permiso.tipoTransaccion===autorizador.tipoTransaccion
            }
          } yield (permiso, autorizador)
          joinPermisosAutorizadores.list groupBy {i => i._1} map {p => (p._1, p._2.map{a => a._2})} toList
        },
        "Consultar permiso transaccional de agente"
      )
  }

  private[this] def guardarAgentesPermiso(permiso: PermisoTransaccionalUsuarioEmpresarial, estaSeleccionado: Boolean, idsAgentes: Option[List[Int]] = None)(implicit s: Session) = {
    if(idsAgentes.isDefined && idsAgentes.get.headOption.isDefined && idsAgentes.get.headOption.get!=0){
      val ids = idsAgentes.get
      val q = for {
        au <- tablaAutorizadores if au.idEncargo === permiso.idEncargo && au.idAgente === permiso.idAgente && au.tipoTransaccion === permiso.tipoTransaccion
      } yield au
      val existentes = q.list.map{_.idAutorizador}
      val nuevos = if(estaSeleccionado && (permiso.tipoPermiso==2 || permiso.tipoPermiso==3)) ids.diff(existentes) else List()
      val removidos = if(estaSeleccionado && (permiso.tipoPermiso==2 || permiso.tipoPermiso==3)) existentes.diff(ids) else existentes
      nuevos foreach {
        id =>
          tablaAutorizadores += PermisoTransaccionalUsuarioEmpresarialAutorizador(permiso.idEncargo, permiso.idAgente, permiso.tipoTransaccion, id)
      }
      removidos foreach { id => q filter {_.idAutorizador===id} delete }
    }
  }

}
