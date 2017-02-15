package co.com.alianza.persistence.repositories

import co.com.alianza.exceptions.PersistenceException
import co.com.alianza.persistence.entities.CustomDriver.simple._
import co.com.alianza.persistence.entities._
import slick.dbio.Effect.Write
import slick.lifted.TableQuery
import slick.profile.FixedSqlAction

import scala.concurrent.{ ExecutionContext, Future }
import scalaz.Validation

/**
 * Created by manuel on 2015
 */
class PermisoTransaccionalRepository(implicit executionContext: ExecutionContext) extends AlianzaRepository {

  val tablaPermisosEncargos = TableQuery[PermisoTransaccionalUsuarioEmpresarialTable]
  val tablaPermisosEncargosAutorizadores = TableQuery[PermisoTransaccionalUsuarioEmpresarialAutorizadorTable]
  val tablaPermisosEncargosAutorizadoresAdmins = TableQuery[PermisoTransaccionalUsuarioEmpresarialAutorizadorAdminTable]
  val tablaPermisos = TableQuery[PermisoAgenteTable]
  val tablaPermisosAutorizadores = TableQuery[PermisoAgenteAutorizadorTable]
  val tablaPermisosAutorizadoresAdmins = TableQuery[PermisoAgenteAutorizadorAdminTable]
  val tablaAgentes = TableQuery[UsuarioEmpresarialTable]

  type permisosEncargos = List[(PermisoAgente, Seq[(Option[PermisoAgenteAutorizador], Option[Boolean])])]
  type permisosGenerales = List[(String, List[(PermisoTransaccionalUsuarioEmpresarial, List[(Option[PermisoTransaccionalUsuarioEmpresarialAutorizador], Option[Boolean])])])]
  type formatoPermisos = (permisosEncargos, permisosGenerales)

  /**
   * Crea, actualiza o borra un permiso general
   *
   * @param permiso Datos permiso
   * @param estaSeleccionado Si esta seleccionado es un permiso para agregar, si no lo esta el permiso debe eliminarse
   * @param idsAgentes Autorizadores
   * @return
   */
  def guardarPermiso(permiso: PermisoAgente, estaSeleccionado: Boolean, idsAgentes: Option[List[Int]] = None, idClienteAdmin: Int): Future[Validation[PersistenceException, Int]] = loan {

    implicit session =>
      val q = tablaPermisos
        .filter(p => p.idAgente === permiso.idAgente && p.tipoTransaccion === permiso.tipoTransaccion)
        .map(p => (p.tipoPermiso, p.montoMaximoTransaccion, p.montoMaximoDiario, p.minimoNumeroPersonas))

      val regMod: Future[Int] =
        if (!estaSeleccionado) {
          val borrado = session.database.run(tablaPermisos.filter(p => p.idAgente === permiso.idAgente && p.tipoTransaccion === permiso.tipoTransaccion).delete)
          guardarAgentesPermiso(permiso, estaSeleccionado, idsAgentes.get, idClienteAdmin)
          borrado
        } else {
          session.database.run(q.update(permiso.tipoPermiso, permiso.montoMaximoTransaccion, permiso.montoMaximoDiario, permiso.minimoNumeroPersonas))
          session.database.run(tablaPermisos += permiso)
          guardarAgentesPermiso(permiso, estaSeleccionado, idsAgentes.get, idClienteAdmin)
          Future { 1 }
        }

      resolveTry(regMod, "Guardar permiso transaccional general de agente")
  }

  /**
   * Crea, actualiza o borra un permiso de un encargo
   *
   * @param permiso Datos permiso
   * @param estaSeleccionado Se encuentra seleccionado?
   * @param idsAgentes Autorizadores
   * @return
   */
  def guardarPermisoEncargo(permiso: PermisoTransaccionalUsuarioEmpresarial, estaSeleccionado: Boolean, idsAgentes: Option[List[Int]] = None,
    idClienteAdmin: Int): Future[Validation[PersistenceException, Int]] = loan {

    implicit session =>

      val permisoViejo = tablaPermisosEncargos.filter(p => p.idEncargo === permiso.idEncargo && p.idAgente === permiso.idAgente &&
        p.tipoTransaccion === permiso.tipoTransaccion)

      val valoresActualizables = permisoViejo.map(p => (p.tipoPermiso, p.montoMaximoTransaccion, p.montoMaximoDiario, p.minimoNumeroPersonas))

      val resultado = if (estaSeleccionado) {
        for {
          permisoViejoF <- session.database.run(permisoViejo.result)
          crearOActualizar <- if (permisoViejoF.isEmpty) {
            session.database.run(tablaPermisosEncargos += permiso)
          } else {
            session.database.run(
              valoresActualizables.update(permiso.tipoPermiso, permiso.montoMaximoTransaccion, permiso.montoMaximoDiario, permiso.minimoNumeroPersonas)
            )
          }
        } yield crearOActualizar
      } else {
        session.database.run(permisoViejo.delete)
      }

      guardarAgentesPermisoEncargo(permiso, estaSeleccionado, idsAgentes, idClienteAdmin)

      resolveTry(resultado, "Guardar permiso transaccional por encargo de agente")
  }

  def consultaPermisosAgenteLogin(idAgente: Int): Future[Validation[PersistenceException, Seq[Int]]] = loan {
    implicit session =>
      val query = tablaPermisos.filter(_.idAgente === idAgente).map(_.tipoTransaccion) ++ tablaPermisosEncargos.filter(_.idAgente === idAgente).map(_.tipoTransaccion)
      val resultTry = session.database.run(query.result)
      resolveTry(resultTry, "Consultar permiso transaccional de agente para login")
  }

  // -----------------------------------  HIZO PARTE DEL REFACTOR --------------------------------------------------------------------------------------------
  private def consultaPermisosEncargos(idAgente: Int) = {

    val agentesFiltrados = tablaPermisosEncargos.filter(_.idAgente === idAgente)

    val autorizadoresActivos = for {
      autorizador <- tablaPermisosEncargosAutorizadores
      agentes <- tablaAgentes.filter(agente => autorizador.idAutorizador === agente.id && agente.estado === 1)
    } yield autorizador

    val permisosEncargosDadosPoraAgentes = for {
      (permisos, autorizador) <- agentesFiltrados.joinLeft(autorizadoresActivos).on((permiso, aut) => permiso.tipoTransaccion === aut.tipoTransaccion && permiso.idAgente === aut.idAgente)
    } yield (permisos, autorizador, false)

    val permisosEncargosDadosPorAdmin = for {
      (permiso, autorizador) <- agentesFiltrados joinLeft tablaPermisosEncargosAutorizadoresAdmins on {
        (permiso, autorizador) => permiso.tipoTransaccion === autorizador.tipoTransaccion && permiso.idAgente === autorizador.idAgente
      }
    } yield (permiso, autorizador, true)

    permisosEncargosDadosPoraAgentes ++ permisosEncargosDadosPorAdmin
  }

  private def consultaPermisosGeneralesAgente(idAgente: Int)(implicit session: Session) = {

    val agentesFiltrados = tablaPermisos.filter(_.idAgente === idAgente)

    val autorizadoresActivos = for {
      autorizador <- tablaPermisosAutorizadores
      agentes <- tablaAgentes.filter(agente => autorizador.idAutorizador === agente.id && agente.estado === 1)
    } yield autorizador

    val permisosGeneralesDadosPoraAgentes = for {
      (permisos, autorizador) <- agentesFiltrados.joinLeft(autorizadoresActivos).on((permiso, aut) => permiso.tipoTransaccion === aut.tipoTransaccion && permiso.idAgente === aut.idAgente)
    } yield (permisos, autorizador, false)

    val permisosGeneralesDadosPorAdmin = for {
      (permiso, autorizador) <- agentesFiltrados joinLeft tablaPermisosAutorizadoresAdmins on {
        (permiso, autorizador) => permiso.tipoTransaccion === autorizador.tipoTransaccion && permiso.idAgente === autorizador.idAgente
      }
    } yield (permiso, autorizador, true)

    permisosGeneralesDadosPoraAgentes ++ permisosGeneralesDadosPorAdmin
  }

  def consultaPermisosAgente(idAgente: Int): Future[Validation[PersistenceException, formatoPermisos]] = loan {
    implicit session =>

      val j = for {
        permisos <- session.database.run(consultaPermisosEncargos(idAgente).result)
        permisosEncargos <- session.database.run(consultaPermisosGeneralesAgente(idAgente).result)
        permisosEspeciales <- estructurarPermisosEncargo(permisosEncargos)
        permisosEspecialesEncargos <- estructuraPermisosGenerales(permisos)
      } yield (permisosEspeciales, permisosEspecialesEncargos)

      resolveTry(j, "Consultar permiso transaccional de agente")
  }

  /**
   * Método que estrutura la consulta de permisos por encargo que tiene un agente
   * @param permisos Resultado de la consulta de permisos de encargos dados por un agente o un admin
   * @return lista de tuplas organizada por encargo, y con informacion (sí existe) de la persona que debe autorizar esa transacción, junto con un Boolean
   *         indicando si este autorizador es un admin o no
   */
  private def estructurarPermisosEncargo(permisos: Seq[(PermisoAgente, Option[PermisoAgenteAutorizador], Boolean)]) = Future {
    permisos
      .groupBy(permiso => permiso._1)
      .map {
        case (encargo, permiso) => (encargo, permiso.map { case (permiso, autorizador, esAdmin) => (autorizador, Option(esAdmin)) }.toList)
      }.toList
  }

  /**
   * Método que estrutura la consulta de permisos generales de un agente
   * @param permisosEncargos Resultado de la consulta de permisos transaccionales dados por un agente o un admin
   * @return Lista de tuplas organizada por permisosTx, y con informacion (sí existe) de la persona que debe autorizar esa transacción, junto con un Boolean
   *         indicando si este autorizador es un admin o no.
   */
  private def estructuraPermisosGenerales(permisosEncargos: Seq[(PermisoTransaccionalUsuarioEmpresarial, Option[PermisoTransaccionalUsuarioEmpresarialAutorizador], Boolean)]) = Future {

    permisosEncargos
      .groupBy(_._1.idEncargo)
      .map {
        case (encargo, permisos) => (encargo, permisos.groupBy(_._1).map {
          case (permisoTx, autorizadores) => (permisoTx, autorizadores.map { case (permiso, autorizador, esAdmin) => (autorizador, Option(esAdmin)) }.toList)
        }
          .toList)
      }.toList
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

      val queryAgentes = tablaPermisosAutorizadores.filter(au => au.idAgente === permiso.idAgente && au.tipoTransaccion === permiso.tipoTransaccion)
      val queryAdmins = tablaPermisosAutorizadoresAdmins.filter(a => a.idAgente === permiso.idAgente && a.tipoTransaccion === permiso.tipoTransaccion)

      for {
        existentesAgentes <- session.database.run(queryAgentes.map(_.idAutorizador).result)
        _ <- insertDeleteAgentes(permiso, estaSeleccionado, ids, queryAgentes, existentesAgentes)
        exitentesAdmin <- session.database.run(queryAdmins.map(_.idAutorizador).result)
        _ <- insertDeleteAdmin(permiso, incluidoClienteAdmin, List(idClienteAdmin), queryAdmins, exitentesAdmin)
      } yield ()
    }
  }

  private def insertDeleteEncargos(permiso: PermisoTransaccionalUsuarioEmpresarial, estaSeleccionado: Boolean, ids: Seq[Int],
    queryAgentes: Query[PermisoTransaccionalUsuarioEmpresarialAutorizadorTable, PermisoTransaccionalUsuarioEmpresarialAutorizador, Seq], existentes: Seq[Int])(implicit session: Session) = {

    val (nuevos, removidos) = if (estaSeleccionado) (ids.diff(existentes), existentes.diff(ids)) else (Seq.empty, existentes)
    val inserts = nuevos.map { id =>
      tablaPermisosEncargosAutorizadores += PermisoTransaccionalUsuarioEmpresarialAutorizador(permiso.idEncargo, permiso.idAgente, permiso.tipoTransaccion, id)
    }
    val deletes = removidos.map(id => queryAgentes.filter(_.idAutorizador === id).delete)
    runInsertDelete(inserts, deletes)
  }

  private def insertDeleteEncargosAdmin(permiso: PermisoTransaccionalUsuarioEmpresarial, estaSeleccionado: Boolean, ids: Seq[Int], existentes: Seq[Int],
    queryAgentes: Query[PermisoTransaccionalUsuarioEmpresarialAutorizadorAdminTable, PermisoTransaccionalUsuarioEmpresarialAutorizador, Seq])(implicit session: Session) = {

    val (nuevos, removidos) = if (estaSeleccionado) (ids.diff(existentes), existentes.diff(ids)) else (Seq.empty, existentes)

    val inserts = nuevos.map { id =>
      tablaPermisosEncargosAutorizadoresAdmins +=
        PermisoTransaccionalUsuarioEmpresarialAutorizador(permiso.idEncargo, permiso.idAgente, permiso.tipoTransaccion, id)
    }
    val deletes = removidos.map(id => queryAgentes.filter(_.idAutorizador === id).delete)
    runInsertDelete(inserts, deletes)
  }

  private[this] def guardarAgentesPermisoEncargo(permiso: PermisoTransaccionalUsuarioEmpresarial, estaSeleccionado: Boolean,
    idsAgentes: Option[List[Int]] = None, idClienteAdmin: Int)(implicit sesssion: Session): Any = {

    val esConAutorizadores = permiso.tipoPermiso == 2 || permiso.tipoPermiso == 3
    val ids = idsAgentes.get.filter { id => id != 0 && id != (-1) }

    if (idsAgentes.isDefined && ids.nonEmpty && idsAgentes.get.headOption.get != 0) {
      val queryAgentes = tablaPermisosEncargosAutorizadores
        .filter(au => au.idEncargo === permiso.idEncargo && au.idAgente === permiso.idAgente && au.tipoTransaccion === permiso.tipoTransaccion)

      for {
        a <- sesssion.database.run(queryAgentes.map(_.idAutorizador).result)
        b <- insertDeleteEncargos(permiso, estaSeleccionado && esConAutorizadores, ids, queryAgentes, a)
      } yield ()
    }

    val adminsIds = if (idsAgentes.get.filter { _ != 0 }.contains(-1)) { List(idClienteAdmin) } else { List() }

    val queryAdmins = tablaPermisosEncargosAutorizadoresAdmins
      .filter(au => au.idEncargo === permiso.idEncargo && au.idAgente === permiso.idAgente && au.tipoTransaccion === permiso.tipoTransaccion)

    for {
      c <- sesssion.database.run(queryAdmins.map(_.idAutorizador).result)
      _ <- insertDeleteEncargosAdmin(permiso, estaSeleccionado && esConAutorizadores, adminsIds, c, queryAdmins)
    } yield ()
  }

}
