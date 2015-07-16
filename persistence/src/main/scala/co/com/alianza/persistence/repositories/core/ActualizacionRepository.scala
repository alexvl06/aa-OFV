package co.com.alianza.persistence.repositories.core

import java.sql.Connection

import co.com.alianza.exceptions.PersistenceException
import co.com.alianza.persistence.messages.ConsultaClienteRequest
import oracle.jdbc.OracleTypes

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try
import scalaz.Validation

/**
 * Created by hernando on 13/07/15.
 */
class ActualizacionRepository(implicit executionContext: ExecutionContext) extends AlianzaCoreRepository{

  //Consulta datos cliente
  def consultaDatosCliente(documento: Int): Future[Validation[PersistenceException, String]] = loan {
    connection =>
      connection.setAutoCommit(false)
      val operation: Try[String] = consultarDatosClienteSP(connection, documento)
      resolveTry(connection,operation,"Consulta Datos Cliente por Número de Identificacón")
  }

  private def consultarDatosClienteSP(conn: Connection, documento: Int) = Try {
    val callString = "{ call sf_qportal_web_clientes.Consultar_Cliente(?,?,?,?,?) }"
    val callableStatement = conn prepareCall callString
    callableStatement registerOutParameter (1, OracleTypes.VARCHAR)
    callableStatement registerOutParameter (2, OracleTypes.VARCHAR)
    callableStatement.setInt    (3, documento)
    callableStatement.setString (4, "C")
    callableStatement registerOutParameter (5, OracleTypes.CURSOR)
    buildSpResponse(conn, callableStatement, 5)
  }

  //Actualizar datos cliente
  def actualizarCliente(msg:ConsultaClienteRequest): Future[Validation[PersistenceException, String]] = loan {
    connection =>
      connection.setAutoCommit(false)
      val operation: Try[String] = actualizarClienteSP(connection,msg)
      resolveTry(connection,operation,"Actualizar Cliente por Número de Identificacón")
  }

  private def actualizarClienteSP(conn: Connection, msg:ConsultaClienteRequest) = Try {
    val callString = "{ call sf_qportal_web.Cliente(?,?,?,?) }"
    val callableStatement = conn prepareCall callString
    callableStatement registerOutParameter (1, OracleTypes.VARCHAR)
    callableStatement registerOutParameter (2, OracleTypes.VARCHAR)
    callableStatement.setString(3, msg.numDocumento)
    callableStatement registerOutParameter (4, OracleTypes.CURSOR)
    buildSpResponse(conn, callableStatement, 4)
  }

  //Lista paises
  def listarPaises : Future[Validation[PersistenceException, String]] = loan {
    connection =>
      connection.setAutoCommit(false)
      val operation: Try[String] = listarPaisesSP(connection)
      resolveTry(connection, operation,"Listar paises")
  }

  private def listarPaisesSP(conn: Connection) = Try {
    val callString = "{ call sf_qportal_web_clientes.Lista_Paises(?,?,?) }"
    val callableStatement = conn prepareCall callString
    callableStatement registerOutParameter (1, OracleTypes.VARCHAR)
    callableStatement registerOutParameter (2, OracleTypes.VARCHAR)
    callableStatement registerOutParameter (3, OracleTypes.CURSOR)
    buildSpResponse(conn, callableStatement, 3)
  }

  //Lista ciudades
  def listarCiudades(pais: Int): Future[Validation[PersistenceException, String]] = loan {
    connection =>
      connection.setAutoCommit(false)
      val operation: Try[String] = listarCiudadesSP(connection, pais)
      resolveTry(connection, operation,"Listar ciudades")
  }

  private def listarCiudadesSP(conn: Connection, pais: Int) = Try {
    val callString = "{ call sf_qportal_web_clientes.Lista_ciudades(?,?,?,?) }"
    val callableStatement = conn prepareCall callString
    callableStatement registerOutParameter (1, OracleTypes.VARCHAR)
    callableStatement registerOutParameter (2, OracleTypes.VARCHAR)
    callableStatement setString (3, pais.toString )
    callableStatement registerOutParameter (4, OracleTypes.CURSOR)
    buildSpResponse(conn, callableStatement, 4)
  }

  //Lista tipo correo
  def listarTipoCorreo: Future[Validation[PersistenceException, String]] = loan {
    connection =>
      connection.setAutoCommit(false)
      val operation: Try[String] = listarTipoCorreoSP(connection)
      resolveTry(connection, operation,"Listar tipo correo")
  }

  private def listarTipoCorreoSP(conn: Connection) = Try {
    val callString = "{ call sf_qportal_web_clientes.Lista_Tipo_Correo(?,?,?) }"
    val callableStatement = conn prepareCall callString
    callableStatement registerOutParameter (1, OracleTypes.VARCHAR)
    callableStatement registerOutParameter (2, OracleTypes.VARCHAR)
    callableStatement registerOutParameter (3, OracleTypes.CURSOR)
    buildSpResponse(conn, callableStatement, 3)
  }

  //Lista envio correspondencia
  def listarEnvioCorrespondencia: Future[Validation[PersistenceException, String]] = loan {
    connection =>
      connection.setAutoCommit(false)
      val operation: Try[String] = listarEnvioCorrespondenciaSP(connection)
      resolveTry(connection, operation,"Listar Envio Correspondencia")
  }

  private def listarEnvioCorrespondenciaSP(conn: Connection) = Try {
    val callString = "{ call sf_qportal_web_clientes.Lista_Envio_Correspondencia(?,?,?) }"
    val callableStatement = conn prepareCall callString
    callableStatement registerOutParameter (1, OracleTypes.VARCHAR)
    callableStatement registerOutParameter (2, OracleTypes.VARCHAR)
    callableStatement registerOutParameter (3, OracleTypes.CURSOR)
    buildSpResponse(conn, callableStatement, 3)
  }

  //Lista ocupaciones
  def listarOcupaciones: Future[Validation[PersistenceException, String]] = loan {
    connection =>
      connection.setAutoCommit(false)
      val operation: Try[String] = listarOcupacionesSP(connection)
      resolveTry(connection, operation,"Listar ocupaciones")
  }

  private def listarOcupacionesSP(conn: Connection) = Try {
    val callString = "{ call sf_qportal_web_clientes.Lista_Ocupaciones(?,?,?) }"
    val callableStatement = conn prepareCall callString
    callableStatement registerOutParameter (1, OracleTypes.VARCHAR)
    callableStatement registerOutParameter (2, OracleTypes.VARCHAR)
    callableStatement registerOutParameter (3, OracleTypes.CURSOR)
    buildSpResponse(conn, callableStatement, 3)
  }

  //Lista actividades economicas
  def listarActividadesEconomicas: Future[Validation[PersistenceException, String]] = loan {
    connection =>
      connection.setAutoCommit(false)
      val operation: Try[String] = listarActividadesEconomicasSP(connection)
      resolveTry(connection, operation,"Listar actividades economicas")
  }

  private def listarActividadesEconomicasSP(conn: Connection) = Try {
    val callString = "{ call sf_qportal_web_clientes.Lista_Actividades_Economicas(?,?,?) }"
    val callableStatement = conn prepareCall callString
    callableStatement registerOutParameter (1, OracleTypes.VARCHAR)
    callableStatement registerOutParameter (2, OracleTypes.VARCHAR)
    callableStatement registerOutParameter (3, OracleTypes.CURSOR)
    buildSpResponse(conn, callableStatement, 3)
  }

}