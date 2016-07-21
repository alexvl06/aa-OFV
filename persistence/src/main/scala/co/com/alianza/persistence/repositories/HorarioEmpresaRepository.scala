package co.com.alianza.persistence.repositories

import co.com.alianza.exceptions.PersistenceException
import co.com.alianza.persistence.entities.CustomDriver.simple._
import co.com.alianza.persistence.entities._

import scala.concurrent.{ ExecutionContext, Future }
import slick.jdbc.JdbcBackend.Session
import slick.lifted.TableQuery
import scala.util.Try
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

  private def obtenerHorarioEmpresaTry(implicit session: Session, idEmpresa: Int): Future[Option[HorarioEmpresa]] =  {
    session.database.run(horarioEmpresa.filter(x => x.idEmpresa === idEmpresa).result.headOption)
  }

  def agregarHorarioEmpresa(horario: HorarioEmpresa) = loan {
    implicit session =>
      val resultTry = Try {
        val query = horarioEmpresa.filter(_.idEmpresa === horario.idEmpresa)
        val result = {
          if (session.database.run(query.result.exists)) query.update(horario)
          else (horarioEmpresa returning horarioEmpresa.map(_.idEmpresa)) += horario
        }
        result > 0
      }
      resolveTry(resultTry, "Agregar Horario Empresa")
  }

}