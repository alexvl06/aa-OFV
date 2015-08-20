package co.com.alianza.persistence.repositories.core

import scala.concurrent.{ExecutionContext, Future}
import scalaz.Validation
import co.com.alianza.exceptions.PersistenceException
import scala.util.Try
import java.sql.Connection
import oracle.jdbc.OracleTypes
import co.com.alianza.persistence.messages.ConsultaClienteRequest


/**
 *
 * @author smontanez
 */
class ClienteRepository( implicit executionContext: ExecutionContext) extends AlianzaCoreRepository{

  def consultaCliente(msg:ConsultaClienteRequest): Future[Validation[PersistenceException, String]] = loan {
    connection =>
      connection.setAutoCommit(false)
      val operation: Try[String] = executeClienteSP(connection,msg)
      resolveTry(connection,operation,"Consulta Cliente por Número de Identificación")
  }

  private def executeClienteSP(conn: Connection, msg:ConsultaClienteRequest) = Try {
    val callString = "{ call sf_qportal_web.Cliente(?,?,?,?) }"
    val callableStatement = conn prepareCall callString
    callableStatement registerOutParameter (1, OracleTypes.VARCHAR)
    callableStatement registerOutParameter (2, OracleTypes.VARCHAR)
    callableStatement.setString(3, msg.numDocumento)
    callableStatement registerOutParameter (4, OracleTypes.CURSOR)
    buildSpResponse(conn, callableStatement, 4)
  }

}