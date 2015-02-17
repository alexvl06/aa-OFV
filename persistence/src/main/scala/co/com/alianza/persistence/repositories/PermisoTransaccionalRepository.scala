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

  val tablaPermisosEncargos = TableQuery[PermisoTransaccionalUsuarioEmpresarialTable]
  val tablaPermisosEncargosAutorizadores = TableQuery[PermisoTransaccionalUsuarioEmpresarialAutorizadorTable]
  val tablaPermisosEncargosAutorizadoresAdmins = TableQuery[PermisoTransaccionalUsuarioEmpresarialAutorizadorAdminTable]
  val tablaPermisos = TableQuery[PermisoAgenteTable]
  val tablaPermisosAutorizadores = TableQuery[PermisoAgenteAutorizadorTable]
  val tablaPermisosAutorizadoresAdmins = TableQuery[PermisoAgenteAutorizadorAdminTable]
  val tablaAgentes = TableQuery[UsuarioEmpresarialTable]

  /**
   * Crea, actualiza o borra un permiso general
   * @param permiso Datos permiso
   * @param estaSeleccionado Se encuentra seleccionado?
   * @param idsAgentes Autorizadores
   * @return
   */
  def guardarPermiso(permiso: PermisoAgente, estaSeleccionado: Boolean, idsAgentes: Option[List[Int]] = None, idClienteAdmin: Int) = loan {
    implicit session =>
      resolveTry(
        Try {
          val q = for {
            p <- tablaPermisos if p.idAgente === permiso.idAgente && p.tipoTransaccion === permiso.tipoTransaccion
          } yield p.minimoNumeroPersonas
          var regMod = 0
          regMod = q update ((permiso.minimoNumeroPersonas))
          if(!estaSeleccionado) {
            guardarAgentesPermiso(permiso, estaSeleccionado, Some(List()), idClienteAdmin)
            regMod = (for {
              p <- tablaPermisos if p.idAgente === permiso.idAgente && p.tipoTransaccion === permiso.tipoTransaccion
            } yield p).delete
            regMod
          }
          if(regMod==0 && estaSeleccionado){
            tablaPermisos += permiso
            guardarAgentesPermiso(permiso, estaSeleccionado, idsAgentes, idClienteAdmin)
            1
          } else {
            guardarAgentesPermiso(permiso, estaSeleccionado, idsAgentes, idClienteAdmin)
            regMod
          }
        },
        "Guardar permiso transaccional general de agente"
      )
  }

  /**
   * Crea, actualiza o borra un permiso de un encargo
   * @param permiso Datos permiso
   * @param estaSeleccionado Se encuentra seleccionado?
   * @param idsAgentes Autorizadores
   * @return
   */
  def guardarPermisoEncargo(permiso: PermisoTransaccionalUsuarioEmpresarial, estaSeleccionado: Boolean, idsAgentes: Option[List[Int]] = None, idClienteAdmin: Int) = loan {
    implicit session =>
      resolveTry(
        Try {
          val q = for {
            p <- tablaPermisosEncargos if p.idEncargo === permiso.idEncargo && p.idAgente === permiso.idAgente && p.tipoTransaccion === permiso.tipoTransaccion
          } yield (p.tipoPermiso, p.montoMaximoTransaccion, p.montoMaximoDiario, p.minimoNumeroPersonas)
          var regMod = 0
          regMod = q update ((permiso.tipoPermiso, permiso.montoMaximoTransaccion, permiso.montoMaximoDiario, permiso.minimoNumeroPersonas))
          if(!estaSeleccionado) {
            guardarAgentesPermisoEncargo(permiso, estaSeleccionado, Some(List()), idClienteAdmin)
            regMod = (for {
              p <- tablaPermisosEncargos if p.idEncargo === permiso.idEncargo && p.idAgente === permiso.idAgente && p.tipoTransaccion === permiso.tipoTransaccion
            } yield p).delete
            regMod
          }
          if(regMod==0 && estaSeleccionado){
            tablaPermisosEncargos += permiso
            guardarAgentesPermisoEncargo(permiso, estaSeleccionado, idsAgentes, idClienteAdmin)
            1
          } else {
            guardarAgentesPermisoEncargo(permiso, estaSeleccionado, idsAgentes, idClienteAdmin)
            regMod
          }
        },
        "Guardar permiso transaccional por encargo de agente"
      )
  }

  def consultaPermisosAgente(idAgente: Int) = loan {
    implicit session =>
      resolveTry(
        Try {
          val joinPermisosTransaccionalesAutorizadores = for {
            ((permiso, autorizador), agente) <- tablaPermisos.filter(_.idAgente===idAgente) leftJoin tablaPermisosAutorizadores on {
              (permiso, autorizador) =>
                permiso.tipoTransaccion===autorizador.tipoTransaccion && permiso.idAgente===autorizador.idAgente
            } join tablaAgentes on {
              case ((permiso, autorizador), agente) =>
                autorizador.idAutorizador===agente.id && agente.estado===1
            }
          } yield (permiso, autorizador.?, false)

          val joinPermisosTransaccionalesAutorizadoresAdmin = for {
            (permiso, autorizador) <- tablaPermisos.filter(_.idAgente===idAgente) leftJoin tablaPermisosAutorizadores on {
              (permiso, autorizador) =>
                permiso.tipoTransaccion===autorizador.tipoTransaccion && permiso.idAgente===autorizador.idAgente
            }
          } yield (permiso, autorizador.?, true)
          val unionPermisos = joinPermisosTransaccionalesAutorizadores ++ joinPermisosTransaccionalesAutorizadoresAdmin

          val joinPermisosTransaccionalesEncargosAutorizadores = for {
            ((permiso, autorizador), agente) <- tablaPermisosEncargos.filter(_.idAgente===idAgente) leftJoin tablaPermisosEncargosAutorizadores on {
              (permiso, autorizador) =>
                permiso.idEncargo===autorizador.idEncargo && permiso.tipoTransaccion===autorizador.tipoTransaccion && permiso.idAgente===autorizador.idAgente
            } join tablaAgentes on {
              case ((permiso, autorizador), agente) =>
                autorizador.idAutorizador===agente.id && agente.estado===1
            }
          } yield (permiso, autorizador.?, false)

          val joinPermisosTransaccionalesEncargosAutorizadoresAdmin = for {
            (permiso, autorizador) <- tablaPermisosEncargos.filter(_.idAgente===idAgente) leftJoin tablaPermisosEncargosAutorizadoresAdmins on {
              (permiso, autorizador) =>
                permiso.idEncargo===autorizador.idEncargo && permiso.tipoTransaccion===autorizador.tipoTransaccion && permiso.idAgente===autorizador.idAgente
            }
          } yield (permiso, autorizador.?, true)
          val unionPermisosEncargos = joinPermisosTransaccionalesEncargosAutorizadores ++ joinPermisosTransaccionalesEncargosAutorizadoresAdmin

          (
            unionPermisos.list groupBy {_._1} map {
              e => ( e._1, e._2.map {a => (a._2, Some(a._3))} toList )
            } toList,
            unionPermisosEncargos.list groupBy {_._1.idEncargo} map {
              e => ( e._1, e._2.groupBy {_._1}.map {a => (a._1, a._2.map{x => (x._2, Some(x._3))})} toList )
            } toList
          )
        },
        "Consultar permiso transaccional de agente"
      )
  }

  private[this] def guardarAgentesPermiso(permiso: PermisoAgente,
                                          estaSeleccionado: Boolean,
                                          idsAgentes: Option[List[Int]] = None,
                                          idClienteAdmin: Int)
                                         (implicit s: Session) = {
    if(idsAgentes.isDefined && idsAgentes.get.headOption.isDefined && idsAgentes.get.headOption.get!=0){
      val ids = idsAgentes.get.filter{id => id!=0 && id!=(-1)}
      val queryAgentes = for {
        au <- tablaPermisosAutorizadores if au.idAgente === permiso.idAgente && au.tipoTransaccion === permiso.tipoTransaccion
      } yield au
      val existentes = queryAgentes.list.map{_.idAutorizador}
      val nuevos = if(estaSeleccionado) ids.diff(existentes) else List()
      val removidos = if(estaSeleccionado) existentes.diff(ids) else existentes
      nuevos foreach {
        id =>
          tablaPermisosAutorizadores += PermisoAgenteAutorizador(permiso.idAgente, permiso.tipoTransaccion, id)
      }
      removidos foreach { id => queryAgentes filter {_.idAutorizador===id} delete }

      val incluidoClienteAdmin = idsAgentes.get.filter{_!=0}.contains(-1)
      val queryAdmins = for {
        au <- tablaPermisosAutorizadoresAdmins if au.idAgente === permiso.idAgente && au.tipoTransaccion === permiso.tipoTransaccion
      } yield au
      val adminsIds = if(incluidoClienteAdmin) List(idClienteAdmin) else List()
      val adminsExistentes = queryAdmins.list.map{_.idAutorizador}
      val adminsNuevos = if(estaSeleccionado) adminsIds.diff(adminsExistentes) else List()
      val adminsRemovidos = if(estaSeleccionado) adminsExistentes.diff(adminsIds) else adminsExistentes
      adminsNuevos foreach {
        id =>
          tablaPermisosAutorizadoresAdmins += PermisoAgenteAutorizador(permiso.idAgente, permiso.tipoTransaccion, id)
      }
      adminsRemovidos foreach { id => queryAdmins filter {_.idAutorizador===id} delete }
    }
  }

  private[this] def guardarAgentesPermisoEncargo(permiso: PermisoTransaccionalUsuarioEmpresarial,
                                          estaSeleccionado: Boolean,
                                          idsAgentes: Option[List[Int]] = None,
                                          idClienteAdmin: Int)
                                         (implicit s: Session) = {
    if(idsAgentes.isDefined && idsAgentes.get.headOption.isDefined && idsAgentes.get.headOption.get!=0){
      val ids = idsAgentes.get.filter{id => id!=0 && id!=(-1)}
      val esConAutorizadores = (permiso.tipoPermiso==2 || permiso.tipoPermiso==3)
      val queryAgentes = for {
        au <- tablaPermisosEncargosAutorizadores if au.idEncargo === permiso.idEncargo && au.idAgente === permiso.idAgente && au.tipoTransaccion === permiso.tipoTransaccion
      } yield au
      val existentes = queryAgentes.list.map{_.idAutorizador}
      val nuevos = if(estaSeleccionado && esConAutorizadores) ids.diff(existentes) else List()
      val removidos = if(estaSeleccionado && esConAutorizadores) existentes.diff(ids) else existentes
      nuevos foreach {
        id =>
          tablaPermisosEncargosAutorizadores += PermisoTransaccionalUsuarioEmpresarialAutorizador(permiso.idEncargo, permiso.idAgente, permiso.tipoTransaccion, id)
      }
      removidos foreach { id => queryAgentes filter {_.idAutorizador===id} delete }

      val incluidoClienteAdmin = idsAgentes.get.filter{_!=0}.contains(-1)
      val queryAdmins = for {
        au <- tablaPermisosEncargosAutorizadoresAdmins if au.idEncargo === permiso.idEncargo && au.idAgente === permiso.idAgente && au.tipoTransaccion === permiso.tipoTransaccion
      } yield au
      val adminsIds = if(incluidoClienteAdmin) List(idClienteAdmin) else List()
      val adminsExistentes = queryAdmins.list.map{_.idAutorizador}
      val adminsNuevos = if(estaSeleccionado && esConAutorizadores) adminsIds.diff(adminsExistentes) else List()
      val adminsRemovidos = if(estaSeleccionado && esConAutorizadores) adminsExistentes.diff(adminsIds) else adminsExistentes
      adminsNuevos foreach {
        id =>
          tablaPermisosEncargosAutorizadoresAdmins += PermisoTransaccionalUsuarioEmpresarialAutorizador(permiso.idEncargo, permiso.idAgente, permiso.tipoTransaccion, id)
      }
      adminsRemovidos foreach { id => queryAdmins filter {_.idAutorizador===id} delete }
    }
  }

}
