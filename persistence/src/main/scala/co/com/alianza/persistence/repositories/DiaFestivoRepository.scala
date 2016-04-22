package co.com.alianza.persistence.repositories

import java.sql.Date
import co.com.alianza.exceptions.PersistenceException
import co.com.alianza.persistence.entities.CustomDriver.simple._
import co.com.alianza.persistence.entities._

import scala.concurrent.{ ExecutionContext, Future }
import scala.slick.jdbc.JdbcBackend.Session
import scala.slick.lifted.TableQuery
import scala.util.Try
import scalaz.{ Validation, Failure => zFailure, Success => zSuccess }

/**
 * @author hernando on 02/03/14.
 */
class DiaFestivoRepository(implicit executionContext: ExecutionContext) extends AlianzaRepository {

  val diaFestivo = TableQuery[DiaFestivoTable]

  def existeDiaFestivo(fecha: Date): Future[Validation[PersistenceException, Boolean]] = loan {
    session =>
      resolveTry(obtenerDiaFestivoTry(session: Session, fecha), "Consulta si existe dia Festivo")
  }

  private def obtenerDiaFestivoTry(implicit session: Session, fecha: Date): Try[Boolean] = Try {
    diaFestivo.filter(x => x.fecha === fecha).exists.run
  }

}