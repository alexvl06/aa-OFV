package co.com.alianza.persistence.repositories

import co.com.alianza.exceptions.PersistenceException
import co.com.alianza.persistence.entities.CustomDriver.simple._
import co.com.alianza.persistence.entities._
import slick.jdbc.JdbcBackend.Session
import slick.lifted.TableQuery

import scala.concurrent.{ ExecutionContext, Future }
import scalaz.{ Validation, Failure => zFailure, Success => zSuccess }

/**
 * @author hernando on 02/03/14.
 */
class HorarioEmpresaRepository(implicit executionContext: ExecutionContext) extends AlianzaRepository {

  val horarioEmpresa = TableQuery[HorarioEmpresaTable]

  def obtenerHorarioEmpresa(idEmpresa: Int): Future[Validation[PersistenceException, Option[HorarioEmpresa]]] = loan {
    session =>
      val resultTry = obtenerHorarioEmpresaTry(session: Session, idEmpresa)
      resolveTry(resultTry, "Consulta horario de una empresa")
  }

  private def obtenerHorarioEmpresaTry(implicit session: Session, idEmpresa: Int): Future[Option[HorarioEmpresa]] = {
    session.database.run(horarioEmpresa.filter(x => x.idEmpresa === idEmpresa).result.headOption)
  }

  def existeHorario(idEmpresa: Int): Future[Validation[PersistenceException, Boolean]] = loan {
    implicit session =>
      val resultTry = session.database.run(horarioEmpresa.filter(_.idEmpresa === idEmpresa).exists.result)
      resolveTry(resultTry, "Agregar Horario Empresa")
  }

  def agregarHorarioEmpresa(horario: HorarioEmpresa, existeHorario: Boolean): Future[Validation[PersistenceException, Boolean]] = loan {
    implicit session =>
      val query = horarioEmpresa.filter(_.idEmpresa === horario.idEmpresa)
      val query2 = if (existeHorario) query.update(horario) else horarioEmpresa returning horarioEmpresa.map(_.idEmpresa) += horario
      val resultTry = session.database.run(query2)

      val resultado = resultTry flatMap {
        case r: Int => Future.successful(existeHorario)
        case err: Any => Future.failed(new Throwable("No se pudo completar la peticion"))
      }
      resolveTry(resultado, "Agregar Horario Empresa")
  }

}