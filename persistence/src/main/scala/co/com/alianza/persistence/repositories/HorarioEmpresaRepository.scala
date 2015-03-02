package co.com.alianza.persistence.repositories

import co.com.alianza.exceptions.PersistenceException
import co.com.alianza.persistence.entities.CustomDriver.simple._
import co.com.alianza.persistence.entities._

import scala.concurrent.{ExecutionContext, Future}
import scala.slick.jdbc.JdbcBackend.Session
import scala.slick.lifted.TableQuery
import scala.util.Try
import scalaz.{Validation, Failure => zFailure, Success => zSuccess}

/**
 * @author hernando on 02/03/14.
 */
class HorarioEmpresaRepository ( implicit executionContext: ExecutionContext) extends AlianzaRepository  {

  val horarioEmpresa = TableQuery[HorarioEmpresaTable]

  def obtenerHorarioEmpresa(idEmpresa : Int): Future[Validation[PersistenceException, Option[HorarioEmpresa]]] = loan {
    session =>
      val resultTry = obtenerHorarioEmpresaTry(session: Session, idEmpresa)
      resolveTry(resultTry, "Consulta horario de una empresa")
  }

  private def obtenerHorarioEmpresaTry(implicit session: Session, idEmpresa : Int): Try[Option[HorarioEmpresa]] = Try {
    horarioEmpresa.filter(x => x.idEmpresa === idEmpresa).list.headOption
  }

  def agregarHorarioEmpresa(horario: HorarioEmpresa): Future[Validation[PersistenceException, Int]] = loan {
    implicit session =>
      val resultTry = Try{ (horarioEmpresa  returning horarioEmpresa.map( _.idEmpresa)) += horario }
      resolveTry(resultTry, "Agregar Horario Empresa")
  }

}