package co.com.alianza.persistence.repositories

import scalaz.Validation
import scala.concurrent.{ ExecutionContext, Future }
import co.com.alianza.persistence.entities._
import co.com.alianza.exceptions.PersistenceException
import CustomDriver.simple._
import slick.dbio.Effect.{ Read, Write }
import slick.lifted.TableQuery
import slick.profile.{ FixedSqlAction, FixedSqlStreamingAction, SqlAction }
import slick.dbio.Effect.Read
import slick.profile.{ FixedSqlStreamingAction, SqlAction }

import scala.util.Try

/**
 * Created by manuel on 8/01/15.
 */
class PermisoTransaccionalRepository(implicit executionContext: ExecutionContext) extends AlianzaRepository {

  val tablaPermisosEncargos = TableQuery[PermisoTransaccionalUsuarioEmpresarialTable]
  val tablaPermisosEncargosAutorizadores = TableQuery[PermisoTransaccionalUsuarioEmpresarialAutorizadorTable]
  val tablaPermisosEncargosAutorizadoresAdmins = TableQuery[PermisoTransaccionalUsuarioEmpresarialAutorizadorAdminTable]
  val tablaPermisos = TableQuery[PermisoAgenteTable]
  val tablaPermisosAutorizadores = TableQuery[PermisoAgenteAutorizadorTable]
  val tablaPermisosAutorizadoresAdmins = TableQuery[PermisoAgenteAutorizadorAdminTable]
  val tablaAgentes = TableQuery[UsuarioEmpresarialTable]

  /**
   * Crea, actualiza o borra un permiso general
   *
   * @param permiso Datos permiso
   * @param estaSeleccionado Se encuentra seleccionado?
   * @param idsAgentes Autorizadores
   * @return
   */

  def guardarPermiso(permiso: PermisoAgente, estaSeleccionado: Boolean, idsAgentes: Option[List[Int]] = None, idClienteAdmin: Int) = loan {
    implicit session =>
      val q = tablaPermisos
        .filter(p => p.idAgente === permiso.idAgente && p.tipoTransaccion === permiso.tipoTransaccion)
        .map(p => (p.tipoPermiso, p.montoMaximoTransaccion, p.montoMaximoDiario, p.minimoNumeroPersonas))

      val regMod: Future[Int] =
        if (!estaSeleccionado) {
          guardarAgentesPermiso(permiso, estaSeleccionado, List(), idClienteAdmin)
          session.database.run(tablaPermisos.filter(p => p.idAgente === permiso.idAgente && p.tipoTransaccion === permiso.tipoTransaccion).delete)
        } else {
          session.database.run(q.update(permiso.tipoPermiso, permiso.montoMaximoTransaccion, permiso.montoMaximoDiario, permiso.minimoNumeroPersonas))
        }

      val result: Future[Int] =
        if (estaSeleccionado) {
          session.database.run(tablaPermisos += permiso)
          guardarAgentesPermiso(permiso, estaSeleccionado, idsAgentes.get, idClienteAdmin)
          Future { 1 }
        } else {
          guardarAgentesPermiso(permiso, estaSeleccionado, idsAgentes.get, idClienteAdmin)
          regMod
        }

      resolveTry(result, "Guardar permiso transaccional general de agente")
  }

  /**
   * Crea, actualiza o borra un permiso de un encargo
   *
   * @param permiso Datos permiso
   * @param estaSeleccionado Se encuentra seleccionado?
   * @param idsAgentes Autorizadores
   * @return
   */
  def guardarPermisoEncargo(permiso: PermisoTransaccionalUsuarioEmpresarial, estaSeleccionado: Boolean, idsAgentes: Option[List[Int]] = None, idClienteAdmin: Int) = loan {
    implicit session =>

      val q = for {
        p <- tablaPermisosEncargos if p.idEncargo === permiso.idEncargo && p.idAgente === permiso.idAgente && p.tipoTransaccion === permiso.tipoTransaccion
      } yield (p.tipoPermiso, p.montoMaximoTransaccion, p.montoMaximoDiario, p.minimoNumeroPersonas)

      val r: Future[Int] =
        if (!estaSeleccionado) {
          guardarAgentesPermisoEncargo(permiso, estaSeleccionado, Some(List()), idClienteAdmin)
          guardarAgentesPermisoEncargo(permiso, estaSeleccionado, idsAgentes, idClienteAdmin)
          session.database.run(tablaPermisosEncargos.filter(p => p.idEncargo === permiso.idEncargo && p.idAgente === permiso.idAgente && p.tipoTransaccion === permiso.tipoTransaccion).delete)
        } else {
          session.database.run(q.update(permiso.tipoPermiso, permiso.montoMaximoTransaccion, permiso.montoMaximoDiario, permiso.minimoNumeroPersonas))
          tablaPermisosEncargos += permiso
          guardarAgentesPermisoEncargo(permiso, estaSeleccionado, idsAgentes, idClienteAdmin)
          Future { 1 }
        }

      resolveTry(r, "Guardar permiso transaccional por encargo de agente")
  }

  def consultaPermisosAgenteLogin(idAgente: Int) = loan {
    implicit session =>
      val query = tablaPermisos.filter(_.idAgente === idAgente).map(_.tipoTransaccion) ++
        tablaPermisosEncargos.filter(_.idAgente === idAgente).map(_.tipoTransaccion)
      val resultTry = session.database.run(query.result)
      resolveTry(resultTry, "Consultar permiso transaccional de agente para login")
  }

  def consultaPermisosAgente(idAgente: Int) = loan {
    implicit session =>

      val joinPermisosTransaccionalesAutorizadores = for {
        ((permiso, autorizador), agente) <- tablaPermisos.filter(_.idAgente === idAgente) joinLeft tablaPermisosAutorizadores on {
          case (permiso, autorizador) => permiso.tipoTransaccion === autorizador.tipoTransaccion && permiso.idAgente === autorizador.idAgente
        } join tablaAgentes on {
          case ((permiso, autorizador), agente) => autorizador.map(n => n.idAutorizador === agente.id && agente.estado === 1)
        }
      } yield (permiso, autorizador, false)

      val joinPermisosTransaccionalesAutorizadoresAdmin = for {
        (permiso, autorizador) <- tablaPermisos.filter(_.idAgente === idAgente) joinLeft tablaPermisosAutorizadoresAdmins on {
          (permiso, autorizador) =>
            permiso.tipoTransaccion === autorizador.tipoTransaccion && permiso.idAgente === autorizador.idAgente
        }
      } yield (permiso, autorizador, true)

      val unionPermisos = joinPermisosTransaccionalesAutorizadores ++ joinPermisosTransaccionalesAutorizadoresAdmin

      val joinPermisosTransaccionalesEncargosAutorizadores = for {
        ((permiso, autorizador), agente) <- tablaPermisosEncargos.filter(_.idAgente === idAgente) joinLeft tablaPermisosEncargosAutorizadores on {
          (permiso, autorizador) =>
            permiso.idEncargo === autorizador.idEncargo && permiso.tipoTransaccion === autorizador.tipoTransaccion && permiso.idAgente === autorizador.idAgente
        } join tablaAgentes on {
          case ((permiso, autorizador), agente) => autorizador.map(n => n.idAutorizador === agente.id && agente.estado === 1)
        }
      } yield (permiso, autorizador, false)

      val joinPermisosTransaccionalesEncargosAutorizadoresAdmin = for {
        (permiso, autorizador) <- tablaPermisosEncargos.filter(_.idAgente === idAgente) joinLeft tablaPermisosEncargosAutorizadoresAdmins on {
          (permiso, autorizador) =>
            permiso.idEncargo === autorizador.idEncargo && permiso.tipoTransaccion === autorizador.tipoTransaccion && permiso.idAgente === autorizador.idAgente
        }
      } yield (permiso, autorizador, true)

      val unionPermisosEncargos = joinPermisosTransaccionalesEncargosAutorizadores ++ joinPermisosTransaccionalesEncargosAutorizadoresAdmin

      val permiso1 = unionPermisos //.groupBy(_._1).map (e => (e._1, e._2.map{ case (a,b,c) => (b, Some(c))}))
      val permiso2 = unionPermisosEncargos //.groupBy(_._1.idEncargo).map(e => (e._1, e._2
      //        .groupBy(_._1)
      //        .map (a => (
      //          a._1,
      //          a._2.map{
      //            case(a,b,c) => (b, Some(c))})))
      // ))

      val j = session.database.run(permiso1.result)
      //val n = session.database.run(permiso2,result)

      resolveTry(j, "Consultar permiso transaccional de agente")
  }

  private def runInsertDelete(
    inserts: Seq[FixedSqlAction[Int, NoStream, Write]], deletes: Seq[FixedSqlAction[Int, NoStream, Write]]
  )(implicit session: Session) = {

    val allInserts = DBIO.seq(inserts: _*)

    val allDeletes = DBIO.seq(deletes: _*)

    val allActions = DBIO.seq(allInserts, allDeletes).transactionally

    session.database.run(allActions)
  }

  private def insertDeleteAdmin(
    permiso: PermisoAgente, estaSeleccionado: Boolean, ids: Seq[Int], queryAgentes: Query[PermisoAgenteAutorizadorAdminTable, PermisoAgenteAutorizador, Seq],
    existentes: Seq[Int]
  )(implicit session: Session) = {
    val (nuevos, removidos) = if (estaSeleccionado) (ids.diff(existentes), existentes.diff(ids)) else (Seq.empty, existentes)
    val inserts = nuevos.map { id =>
      tablaPermisosAutorizadoresAdmins += PermisoAgenteAutorizador(permiso.idAgente, permiso.tipoTransaccion, id)
    }
    val deletes = removidos.map(id => queryAgentes.filter(_.idAutorizador === id).delete)

    runInsertDelete(inserts, deletes)
  }

  private def insertDeleteAgentes(
    permiso: PermisoAgente, estaSeleccionado: Boolean, ids: Seq[Int], queryAgentes: Query[PermisoAgenteAutorizadorTable, PermisoAgenteAutorizador, Seq],
    existentes: Seq[Int]
  )(implicit session: Session) = {
    val (nuevos, removidos) = if (estaSeleccionado) (ids.diff(existentes), existentes.diff(ids)) else (Seq.empty, existentes)
    val inserts = nuevos.map { id =>
      tablaPermisosAutorizadores += PermisoAgenteAutorizador(permiso.idAgente, permiso.tipoTransaccion, id)
    }
    val deletes = removidos.map(id => queryAgentes.filter(_.idAutorizador === id).delete)
    runInsertDelete(inserts, deletes)
  }

  private[this] def guardarAgentesPermiso(permiso: PermisoAgente, estaSeleccionado: Boolean, idsAgentes: Seq[Int], idClienteAdmin: Int)(implicit session: Session) = {

    if (idsAgentes.nonEmpty) {
      val ids = idsAgentes.filter { id => id != 0 && id != (-1) }
      val incluidoClienteAdmin = idsAgentes.filter { _ != 0 }.contains(-1)

      val queryAgentes = tablaPermisosAutorizadores
        .filter(au => au.idAgente === permiso.idAgente && au.tipoTransaccion === permiso.tipoTransaccion)

      val queryAdmins = tablaPermisosAutorizadoresAdmins
        .filter(a => a.idAgente === permiso.idAgente && a.tipoTransaccion === permiso.tipoTransaccion)

      for {
        existentesAgentes <- session.database.run(queryAgentes.map(_.idAutorizador).result)
        _ <- insertDeleteAgentes(permiso, estaSeleccionado, ids, queryAgentes, existentesAgentes)
        exitentesAdmin <- session.database.run(queryAdmins.map(_.idAutorizador).result)
        _ <- insertDeleteAdmin(permiso, incluidoClienteAdmin, List(idClienteAdmin), queryAdmins, exitentesAdmin)
      } yield ()
    }
  }

  private def insertDeleteEncargos(
    permiso: PermisoTransaccionalUsuarioEmpresarial, estaSeleccionado: Boolean, ids: Seq[Int],
    queryAgentes: Query[PermisoTransaccionalUsuarioEmpresarialAutorizadorTable, PermisoTransaccionalUsuarioEmpresarialAutorizador, Seq], existentes: Seq[Int]
  )(implicit session: Session) = {

    val (nuevos, removidos) = if (estaSeleccionado) (ids.diff(existentes), existentes.diff(ids)) else (Seq.empty, existentes)
    val inserts = nuevos.map { id =>
      tablaPermisosEncargosAutorizadores += PermisoTransaccionalUsuarioEmpresarialAutorizador(permiso.idEncargo, permiso.idAgente, permiso.tipoTransaccion, id)
    }
    val deletes = removidos.map(id => queryAgentes.filter(_.idAutorizador === id).delete)
    runInsertDelete(inserts, deletes)
  }

  private def insertDeleteEncargosAdmin(
    permiso: PermisoTransaccionalUsuarioEmpresarial, estaSeleccionado: Boolean, ids: Seq[Int], existentes: Seq[Int],
    queryAgentes: Query[PermisoTransaccionalUsuarioEmpresarialAutorizadorAdminTable, PermisoTransaccionalUsuarioEmpresarialAutorizador, Seq]
  )(implicit session: Session) = {

    val (nuevos, removidos) = if (estaSeleccionado) (ids.diff(existentes), existentes.diff(ids)) else (Seq.empty, existentes)
    val inserts = nuevos.map { id =>
      tablaPermisosEncargosAutorizadoresAdmins +=
        PermisoTransaccionalUsuarioEmpresarialAutorizador(permiso.idEncargo, permiso.idAgente, permiso.tipoTransaccion, id)
    }
    val deletes = removidos.map(id => queryAgentes.filter(_.idAutorizador === id).delete)
    runInsertDelete(inserts, deletes)
  }

  private[this] def guardarAgentesPermisoEncargo(
    permiso: PermisoTransaccionalUsuarioEmpresarial, estaSeleccionado: Boolean, idsAgentes: Option[List[Int]] = None, idClienteAdmin: Int
  )(implicit sesssion: Session) = {

    if (idsAgentes.isDefined && idsAgentes.nonEmpty && idsAgentes.get.headOption.get != 0) {
      val ids = idsAgentes.get.filter { id => id != 0 && id != (-1) }
      val esConAutorizadores = permiso.tipoPermiso == 2 || permiso.tipoPermiso == 3
      val queryAgentes = tablaPermisosEncargosAutorizadores
        .filter(au => au.idEncargo === permiso.idEncargo && au.idAgente === permiso.idAgente && au.tipoTransaccion === permiso.tipoTransaccion)

      val incluidoClienteAdmin = idsAgentes.get.filter { _ != 0 }.contains(-1)
      val queryAdmins = tablaPermisosEncargosAutorizadoresAdmins
        .filter(au => au.idEncargo === permiso.idEncargo && au.idAgente === permiso.idAgente && au.tipoTransaccion === permiso.tipoTransaccion)

      val adminsIds = if (incluidoClienteAdmin) List(idClienteAdmin) else List()

      for {
        a <- sesssion.database.run(queryAgentes.map(_.idAutorizador).result)
        b <- insertDeleteEncargos(permiso, estaSeleccionado && esConAutorizadores, ids, queryAgentes, a)
        c <- sesssion.database.run(queryAdmins.map(_.idAutorizador).result)
        _ <- insertDeleteEncargosAdmin(permiso, estaSeleccionado && esConAutorizadores, ids, c, queryAdmins)
      } yield ()

    }
  }

}
