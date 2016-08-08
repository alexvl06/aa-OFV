package co.com.alianza.persistence.repositories.core

import scala.concurrent.{ ExecutionContext, Future }
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
class ClienteRepository(implicit executionContext: ExecutionContext) extends AlianzaCoreRepository {

  def consultaCliente(numDocumento: String): Future[Validation[PersistenceException, String]] = loan {
    connection =>
      connection.setAutoCommit(false)
      val operation: Try[String] = executeClienteSP(connection, numDocumento)
      resolveTry(connection, operation, "Consulta Cliente por Número de Identificación")
  }

  private def executeClienteSP(conn: Connection, numDocumento: String): Try[String] = Try {
    val callString = "{ call sf_qportal_web.Cliente(?,?,?,?) }"
    val callableStatement = conn prepareCall callString
    callableStatement registerOutParameter (1, OracleTypes.VARCHAR)
    callableStatement registerOutParameter (2, OracleTypes.VARCHAR)
    callableStatement.setString(3, numDocumento)
    callableStatement registerOutParameter (4, OracleTypes.CURSOR)
    buildSpResponse(conn, callableStatement, 4)
  }

  def consultaGrupo(idGrupo: Int): Future[Validation[PersistenceException, String]] = loan {
    connection =>
      connection.setAutoCommit(false)
      val operation: Try[String] = executeGrupoSP(connection, idGrupo)
      resolveTry(connection, operation, "Consulta Grupo por id del grupo.")
  }

  private def executeGrupoSP(conn: Connection, idGrupo: Int) = Try {
    val callString = "{ call sf_qportal_web.validar_grupo_cliente(?,?,?,?) }"
    val callableStatement = conn prepareCall callString
    callableStatement registerOutParameter (1, OracleTypes.VARCHAR)
    callableStatement registerOutParameter (2, OracleTypes.VARCHAR)
    callableStatement.setInt(3, idGrupo)
    callableStatement registerOutParameter (4, OracleTypes.CURSOR)
    buildSpResponse(conn, callableStatement, 4)
  }

}