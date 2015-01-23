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
  val tablaAutorizadoresAdmins = TableQuery[PermisoTransaccionalUsuarioEmpresarialAutorizadorAdminTable]

  /**
   * Crea, actualiza o borra un permiso
   * @param permiso Datos permiso
   * @param estaSeleccionado Se encuentra seleccionado?
   * @param idsAgentes Autorizadores
   * @return
   */
  def guardarPermiso(permiso: PermisoTransaccionalUsuarioEmpresarial, estaSeleccionado: Boolean, idsAgentes: Option[List[Int]] = None, idClienteAdmin: Int) = loan {
    implicit session =>
      resolveTry(
        Try {
          val q = for {
            p <- tabla if p.idEncargo === permiso.idEncargo && p.idAgente === permiso.idAgente && p.tipoTransaccion === permiso.tipoTransaccion
          } yield (p.tipoPermiso, p.montoMaximoTransaccion, p.montoMaximoDiario, p.minimoNumeroPersonas)
          var regMod = 0
          regMod = q update ((permiso.tipoPermiso, permiso.montoMaximoTransaccion, permiso.montoMaximoDiario, permiso.minimoNumeroPersonas))
          if(!estaSeleccionado) {
            guardarAgentesPermiso(permiso, estaSeleccionado, Some(List()), idClienteAdmin)
            regMod = (for {
              p <- tabla if p.idEncargo === permiso.idEncargo && p.idAgente === permiso.idAgente && p.tipoTransaccion === permiso.tipoTransaccion
            } yield p).delete
            regMod
          }
          if(regMod==0 && estaSeleccionado){
            tabla += permiso
            guardarAgentesPermiso(permiso, estaSeleccionado, idsAgentes, idClienteAdmin)
            1
          } else {
            guardarAgentesPermiso(permiso, estaSeleccionado, idsAgentes, idClienteAdmin)
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
            (permiso, autorizador) <- tabla.filter(_.idAgente===idAgente) leftJoin tablaAutorizadores on {
              (permiso, autorizador) =>
                permiso.idEncargo===autorizador.idEncargo && permiso.tipoTransaccion===autorizador.tipoTransaccion && permiso.idAgente===autorizador.idAgente
            }
          } yield (permiso, autorizador.?)
          joinPermisosAutorizadores.list groupBy {_._1.idEncargo} map {
            e => ( e._1, e._2.groupBy {_._1}.map {a => (a._1, a._2.map{_._2})} toList )
          } toList
        },
        "Consultar permiso transaccional de agente"
      )
  }

  private[this] def guardarAgentesPermiso(permiso: PermisoTransaccionalUsuarioEmpresarial,
                                          estaSeleccionado: Boolean,
                                          idsAgentes: Option[List[Int]] = None,
                                          idClienteAdmin: Int)
                                         (implicit s: Session) = {
    if(idsAgentes.isDefined && idsAgentes.get.headOption.isDefined && idsAgentes.get.headOption.get!=0){
      val ids = idsAgentes.get.filter{id => id!=0 && id!=(-1)}
      val esConAutorizadores = (permiso.tipoPermiso==2 || permiso.tipoPermiso==3)
      val queryAgentes = for {
        au <- tablaAutorizadores if au.idEncargo === permiso.idEncargo && au.idAgente === permiso.idAgente && au.tipoTransaccion === permiso.tipoTransaccion
      } yield au
      val existentes = queryAgentes.list.map{_.idAutorizador}
      val nuevos = if(estaSeleccionado && esConAutorizadores) ids.diff(existentes) else List()
      val removidos = if(estaSeleccionado && esConAutorizadores) existentes.diff(ids) else existentes
      nuevos foreach {
        id =>
          tablaAutorizadores += PermisoTransaccionalUsuarioEmpresarialAutorizador(permiso.idEncargo, permiso.idAgente, permiso.tipoTransaccion, id)
      }
      removidos foreach { id => queryAgentes filter {_.idAutorizador===id} delete }

      val incluidoClienteAdmin = idsAgentes.get.filter{_!=0}.contains(-1)
      val queryAdmins = for {
        au <- tablaAutorizadoresAdmins if au.idEncargo === permiso.idEncargo && au.idAgente === permiso.idAgente && au.tipoTransaccion === permiso.tipoTransaccion
      } yield au
      val adminsIds = if(incluidoClienteAdmin) List(idClienteAdmin) else List()
      val adminsExistentes = queryAdmins.list.map{_.idAutorizador}
      val adminsNuevos = if(estaSeleccionado && esConAutorizadores) adminsIds.diff(adminsExistentes) else List()
      val adminsRemovidos = if(estaSeleccionado && esConAutorizadores) adminsExistentes.diff(adminsIds) else adminsExistentes
      adminsNuevos foreach {
        id =>
          tablaAutorizadoresAdmins += PermisoTransaccionalUsuarioEmpresarialAutorizadorAdmin(permiso.idEncargo, permiso.idAgente, permiso.tipoTransaccion, id)
      }
      adminsRemovidos foreach { id => queryAdmins filter {_.idAutorizadorAdmin===id} delete }
    }
  }

}
