package co.com.alianza.persistence.repositories

import scala.concurrent.{ Await, ExecutionContext, Future }
import scala.util.{ Failure, Success, Try }
import scalaz.{ Validation, Failure => zFailure, Success => zSuccess }
import co.com.alianza.exceptions.{ LevelException, PersistenceException, TechnicalLevel, TimeoutLevel }
import portal.transaccional.fiduciaria.autenticacion.storage.conn.pg.DataBaseAccessPGAlianza
import slick.jdbc.JdbcBackend.Database
import slick.jdbc.JdbcBackend.Session
//import com.mchange.v2.resourcepool.TimeoutException
import java.net.SocketTimeoutException
//import co.com.alianza.persistence.entities.CustomDriver.simple

/**
 * Repositorio que obtiene la session slick del DataSource: [[DataBaseAccessPGAlianza.dataSource]]
 *
 * @author seven4n
 */
class AlianzaRepository(implicit val executionContex: ExecutionContext) {

  def loan[R](f: Session => Validation[PersistenceException, R]): Future[Validation[PersistenceException, R]] = {
    Future {
      Try {
        Database.forDataSource(DataBaseAccessPGAlianza.dataSource) createSession ()
      } match {
        case Success(session) =>
          f(session) map {
            value =>
              session.close()
              value
          }
        case Failure(exception) =>
          //   log.error
          zFailure(PersistenceException(exception, getLevelException(exception), exception.getMessage))
      }
    }
  }

  private def getLevelException(exception: Throwable): LevelException = {
    exception.getCause match {
      case ex: SocketTimeoutException => TimeoutLevel
      case _ => TechnicalLevel
    }
  }

  def resolveTry[T](operation: Future[T], messageInfo: String): Validation[PersistenceException, T] = {
    import scala.concurrent.duration._
    //TODO: QUITAR ESTO POR FAVOR !!
    Await.result(operation, 120.seconds)
    operation.value match {
      case Some(e) => e match {
        case scala.util.Success(response) =>
          //TODO:Agregar logs
          //log.info
          //log.debug
          zSuccess(response)
        case scala.util.Failure(exception) =>
          //log.error
          exception.printStackTrace()
          zFailure(PersistenceException(exception, getLevelException(exception), exception.getMessage))
      }
      case _ =>
        val exception = new Exception
        zFailure(PersistenceException(exception, getLevelException(exception), exception.getMessage))
    }
  }

}