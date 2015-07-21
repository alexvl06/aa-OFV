package co.com.alianza.persistence.repositories.core

import java.sql.Connection

import co.com.alianza.exceptions.PersistenceException
import co.com.alianza.persistence.messages.ActualizacionRequest
import oracle.jdbc.OracleTypes

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try
import scalaz.Validation

/**
 * Created by hernando on 13/07/15.
 */
class ActualizacionRepository(implicit executionContext: ExecutionContext) extends AlianzaCoreRepository{

  //Consulta datos cliente
  def consultaDatosCliente(documento: String, tipoDocumento: String): Future[Validation[PersistenceException, String]] = loan {
    connection =>
      connection.setAutoCommit(false)
      val operation: Try[String] = consultarDatosClienteSP(connection, documento, tipoDocumento)
      resolveTry(connection,operation,"Consulta Datos Cliente por Número de Identificacón")
  }

  private def consultarDatosClienteSP(conn: Connection, documento: String, tipoDocumento: String) = Try {
    val callString = "{ call sf_qportal_web_clientes.Consultar_Cliente(?,?,?,?,?) }"
    val callableStatement = conn prepareCall callString
    callableStatement registerOutParameter (1, OracleTypes.VARCHAR)
    callableStatement registerOutParameter (2, OracleTypes.VARCHAR)
    callableStatement.setString(3, documento)
    callableStatement.setString(4, tipoDocumento)
    callableStatement registerOutParameter (5, OracleTypes.CURSOR)
    buildSpResponse(conn, callableStatement, 5)
  }

  //Actualizar datos cliente
  def actualizarCliente(msg: ActualizacionRequest): Future[Validation[PersistenceException, String]] = loan {
    connection =>
      connection.setAutoCommit(false)
      val operation: Try[String] = actualizarClienteSP(connection,msg)
      resolveTry(connection,operation,"Actualizar Cliente")
  }

  private def actualizarClienteSP(conn: Connection, msg: ActualizacionRequest) = Try {
    val callString = "{ call sf_qportal_web_clientes.Actualizar_Cliente(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) }"
    val callableStatement = conn prepareCall callString
    callableStatement.registerOutParameter (1, OracleTypes.VARCHAR)
    callableStatement.registerOutParameter (2, OracleTypes.VARCHAR)
    callableStatement.setString(3, msg.nro_identifica)
    callableStatement.setString(4, msg.tpident)
    callableStatement.setString(5, msg.fdpn_nombre1)
    callableStatement.setString(6, msg.fdpn_nombre2)
    callableStatement.setString(7, msg.fdpn_apell1)
    callableStatement.setString(8, msg.fdpn_apell2)
    callableStatement.setString(9, msg.fdpn_pais_residencia)
    callableStatement.setString(10, msg.fdpn_drcl_dire_res)
    callableStatement.setString(11, msg.fdpn_drcl_dire_ofi)
    callableStatement.setString(12, msg.fdpn_drcl_ciud_res)
    callableStatement.setString(13, msg.fdpn_drcl_tele_res)
    callableStatement.setString(14, msg.fdpn_dcfd_email)//fdpn_dcfd_email_ant
    callableStatement.setString(15, msg.fdpn_dcfd_email)
    callableStatement.setString(16, msg.fdpn_dcfd_tipo)//fdpn_dcfd_tipo_ant
    callableStatement.setString(17, msg.fdpn_dcfd_tipo)
    callableStatement.setString(18, msg.fdpn_envio_corresp)
    callableStatement.setString(19, msg.fdpn_telefono_movil_1)
    callableStatement.setString(20, msg.fdpn_pais_tel_mov_1)
    callableStatement.setString(21, msg.datosEmp.fdpn_ocupacion)
    callableStatement.setString(22, msg.datosEmp.fdpn_if_declara_renta)
    callableStatement.setString(23, msg.datosEmp.fdpn_pafd_pais)//fdpn_pafd_pais_ant
    callableStatement.setString(24, msg.datosEmp.fdpn_pafd_pais)
    callableStatement.setString(25, msg.datosEmp.fdpn_ciua)
    callableStatement.setString(26, msg.datosEmp.fdpn_nombre_emp)
    callableStatement.setString(27, msg.datosEmp.fdpn_nit_emp)
    callableStatement.setString(28, msg.datosEmp.fdpn_cargo)
    callableStatement.setString(29, msg.datosEmp.fdpn_dire_emp)
    callableStatement.setString(30, msg.datosEmp.fdpn_ciud_emp)
    callableStatement.setString(31, msg.datosEmp.fdpn_tele_emp)
    callableStatement.setString(32, msg.datosEmp.fdpn_if_vactivos)
    callableStatement.setString(33, msg.datosEmp.fdpn_if_vpasivos)
    callableStatement.setString(34, msg.datosEmp.fdpn_if_vpatrimonio)
    callableStatement.setString(35, msg.datosEmp.fdpn_if_vingresos)
    callableStatement.setString(36, msg.datosEmp.fdpn_if_vegresos)
    callableStatement.setString(37, msg.datosEmp.fdpn_if_vingresos_noop_mes)
    buildSpResponse(conn, callableStatement)
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