package co.com.alianza.persistence.repositories.core

import java.sql.{ CallableStatement, Connection, ResultSet }

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Success, Try }
import scalaz.{ Validation, Failure => zFailure, Success => zSuccess }
import co.com.alianza.exceptions._
import oracle.net.ns.NetException
import java.net.SocketTimeoutException

import portal.transaccional.fiduciaria.autenticacion.storage.conn.oracle.DataBaseAccessOracleAlianza

/**
 * Repositorio que obtiene la conexión del DataSource: [[portal.transaccional.fiduciaria.autenticacion.storage.conn.oracle.DataBaseAccessOracleAlianza.ds]]
 *
 * @author seven4n
 */
class AlianzaCoreRepository(implicit val executionContex: ExecutionContext) {

  def loan[R](f: Connection => Validation[PersistenceException, R]): Future[Validation[PersistenceException, R]] = {
    Future {
      Try {
        val connectionDataSource = DataBaseAccessOracleAlianza.ds
        connectionDataSource.getConnection
      } match {
        case Success(connection) =>
          // log.info
          // log.debug
          f(connection) map {
            value =>
              connection.close()
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
      case ex: NetException =>
        ex.getCause match {
          case ex: SocketTimeoutException => TimeoutLevel
          case _ => NetworkLevel
        }
      case _ => TechnicalLevel
    }
  }

  def resolveTry[T](connection: Connection, operation: Try[T], messageInfo: String): Validation[PersistenceException, T] = {
    operation match {
      case Success(response) =>
        //TODO:Agregar logs
        //log.info
        //log.debug
        connection.commit()
        connection.close()
        zSuccess(response)
      case Failure(exception: PersistenceException) =>
        connection.close()
        exception.printStackTrace()
        zFailure(exception)
      case Failure(exception) =>
        connection.close()
        exception.printStackTrace()
        //log.error
        zFailure(PersistenceException(exception, TechnicalLevel, exception.getMessage))
    }
  }

  def buildSpResponse(connection: Connection, callableStatement: CallableStatement, positionResult: Int): String = {
    callableStatement.execute()

    val resultObject = callableStatement getObject positionResult
    resultObject match {
      case r: ResultSet =>
        val result = new StringBuilder()
        var record: String = ""
        val numCol = r.getMetaData.getColumnCount
        while (r next ()) {
          for (a <- 1 to numCol) {
            val x = if (r.getString(a) != null) r.getString(a).replaceAll("\"", "'") else ""
            if (a == 1)
              record = record + "{\n"
            if (a == numCol)
              record = record + "\t\t\"" + r.getMetaData.getColumnName(a).toLowerCase + "\": " + "\"" + x + "\"\n},\n"
            else
              record = record + "\t\t\"" + r.getMetaData.getColumnName(a).toLowerCase + "\": " + "\"" + x + "\",\n"
          }
        }
        if (!record.isEmpty) {
          result append "[" append "\n"
          record = record.substring(0, record.length() - 2)
          result append "\t" + record + "\n"
          result append "]" append "\n"
        } else {
          result append "[" append "\n"
          result append "]" append "\n"
        }

        r.close()
        if (callableStatement != null) callableStatement.close()

        result.toString()

      case r: String => r

      case _ =>
        val codeError = callableStatement getObject 1
        val detailError = callableStatement getObject 2
        val msg = s"Error ejecutando prodecimiento $codeError - $detailError"
        throw PersistenceException(new Exception(msg), BusinessLevel, msg)
    }
  }

  def buildSpResponse(connection: Connection, callableStatement: CallableStatement): String = {
    callableStatement.execute()
    val codeResponse = callableStatement getObject 1
    val detailResponse = callableStatement getObject 2
    detailResponse match {
      case null => codeResponse.toString
      case _ =>
        val msg = s"Error ejecutando prodecimiento $codeResponse - $detailResponse"
        throw PersistenceException(new Exception(codeResponse.toString), BusinessLevel, msg)
    }
  }

}