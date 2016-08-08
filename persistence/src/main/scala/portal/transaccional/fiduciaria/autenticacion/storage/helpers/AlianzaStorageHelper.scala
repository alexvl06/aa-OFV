package portal.transaccional.fiduciaria.autenticacion.storage.helpers

import java.net.SocketTimeoutException
import java.sql.{ CallableStatement, Connection, ResultSet }

import co.com.alianza.exceptions._
import oracle.net.ns.NetException
import org.apache.commons.lang3.StringEscapeUtils
import portal.transaccional.fiduciaria.autenticacion.storage.conn.oracle.DataBaseAccesOracleAlianza
import shapeless.syntax.typeable._
import spray.json.{ JsArray, JsObject, JsString }

import scala.collection.mutable
import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Success, Try }

/**
 * Created by alexandra on 25/07/16.
 */
trait AlianzaStorageHelper {

  implicit val ec: ExecutionContext

  protected def transaction[R](f: Connection => R): Future[R] = {
    Future {
      val connectionDataSource = DataBaseAccesOracleAlianza.ds
      val connection = connectionDataSource.getConnection
      val result = f(connection)
      connection.commit()
      connection.close()
      result
    }.recoverWith {
      case e =>
        e.printStackTrace()
        val fail = e.cast[PersistenceException].getOrElse(PersistenceException(e, getLevelException(e), e.getMessage))
        Future.failed[R](fail)
    }
  }

  protected def buildResult(connection: Connection, callableStatement: CallableStatement, positionResult: Int): Try[String] = {
    Try {
      callableStatement.execute()
      val result = callableStatement.getObject(positionResult)
      result match {
        case r: ResultSet =>
          val records = buildJsFromResult(r)
          r.close()
          records

        case r: String => r
        case r: java.lang.Long => r.toString
        case _ => result
      }
    }.flatMap {
      case r: String => Success(r)
      case _ =>
        val codeError = callableStatement getObject 1
        val detailError = callableStatement getObject 2
        val msg = s"Error ejecutando procedimiento $codeError - $detailError"
        Failure[String](PersistenceException(new Exception(msg), BusinessLevel, msg))
    }
  }

  private def getLevelException(exception: Throwable): LevelException = {
    Option(exception.getCause)
      .collect { case e: NetException if e.getCause.cast[SocketTimeoutException].nonEmpty => TimeoutLevel }
      .getOrElse(TechnicalLevel)
  }

  protected def buildResult(result: Any): Unit = {
    result match {
      case r: ResultSet =>
        val records = buildJsFromResult(r)
        records

      case r: String => r
      case r: java.lang.Long => r.toString
      case _ => result
    }
  }

  /**
   * Construye un arreglo de objetos json [[JsArray]] a partir de un [[ResultSet]].
   */
  private def buildJsFromResult(r: ResultSet) = {
    val recordsList = mutable.MutableList.empty[JsObject]
    val numCol = r.getMetaData.getColumnCount
    while (r next ()) {
      val fields = Seq.tabulate(numCol) { indexUp =>
        val index = indexUp + 1
        val field = r.getMetaData.getColumnName(index).toLowerCase
        val fieldValue = formatField(r.getString(index))
        (field, JsString(fieldValue))
      }
      val record = JsObject(fields.toMap)
      recordsList += record
    }
    JsArray(recordsList.toVector).prettyPrint
  }

  private def formatField(value: String) = {
    Option(value)
      .map(v => StringEscapeUtils.escapeEcmaScript(v.replaceAll("""\t""", "&#x9;").replaceAll("\'", "\\.")))
      .getOrElse("")
  }

}
