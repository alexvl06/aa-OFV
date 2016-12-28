package portal.transaccional.fiduciaria.autenticacion.storage.daos.core

import oracle.jdbc.OracleTypes
import portal.transaccional.fiduciaria.autenticacion.storage.config.DBConfig
import portal.transaccional.fiduciaria.autenticacion.storage.helpers.AlianzaStorageHelper

import scala.concurrent.{ ExecutionContext, Future }

/**
 * Created by hernando on 10/10/16.
 */
case class ActualizacionDAO()(implicit val ec: ExecutionContext, dcConfig: DBConfig) extends AlianzaStorageHelper {

  //Consulta datos cliente
  def consultaDatosCliente(documento: String, tipoDocumento: String): Future[String] = transaction {
    connection =>
      connection.setAutoCommit(false)
      val callString = "{ call sf_qportal_web_clientes.Consultar_Cliente(?,?,?,?,?) }"
      val callableStatement = connection prepareCall callString
      callableStatement registerOutParameter (1, OracleTypes.VARCHAR)
      callableStatement registerOutParameter (2, OracleTypes.VARCHAR)
      callableStatement.setString(3, documento)
      callableStatement.setString(4, tipoDocumento)
      callableStatement registerOutParameter (5, OracleTypes.CURSOR)
      buildResult(connection, callableStatement, 5).get
  }

  //Actualizar datos cliente
  def actualizarCliente(nro_identifica: String, tpident: String, fdpn_nombre1: String, fdpn_nombre2: String, fdpn_apell1: String, fdpn_apell2: String,
    fdpn_pais_residencia: String, fdpn_drcl_dire_res: String, fdpn_drcl_dire_ofi: String, fdpn_drcl_ciud_res: String,
    fdpn_drcl_tele_res: String, fdpn_dcfd_email: String, fdpn_dcfd_email_ant: String, fdpn_dcfd_tipo: String, fdpn_dcfd_tipo_ant: String,
    fdpn_envio_corresp: String, fdpn_telefono_movil_1: String, fdpn_pais_tel_mov_1: String, fdpn_ocupacion: String,
    fdpn_if_declara_renta: String, fdpn_pafd_pais: String, fdpn_pafd_pais_ant: String, fdpn_ciua: String, fdpn_nombre_emp: String,
    fdpn_nit_emp: String, fdpn_cargo: String, fdpn_dire_emp: String, fdpn_ciud_emp: String, fdpn_ciud_nombre_emp: String,
    fdpn_tele_emp: String, fdpn_if_vactivos: String, fdpn_if_vpasivos: String, fdpn_if_vpatrimonio: String, fdpn_if_vingresos: String,
    fdpn_if_vegresos: String, fdpn_if_vingresos_noop_mes: String): Future[String] = transaction {
    connection =>
      connection.setAutoCommit(false)
      val callString = "{ call sf_qportal_web_clientes.Actualizar_Cliente(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) }"
      val callableStatement = connection prepareCall callString
      callableStatement.registerOutParameter(1, OracleTypes.VARCHAR)
      callableStatement.registerOutParameter(2, OracleTypes.VARCHAR)
      callableStatement.setString(3, nro_identifica)
      callableStatement.setString(4, tpident)
      callableStatement.setString(5, fdpn_nombre1)
      callableStatement.setString(6, fdpn_nombre2)
      callableStatement.setString(7, fdpn_apell1)
      callableStatement.setString(8, fdpn_apell2)
      callableStatement.setString(9, fdpn_pais_residencia)
      callableStatement.setString(10, fdpn_drcl_dire_res)
      callableStatement.setString(11, fdpn_drcl_dire_ofi)
      callableStatement.setString(12, fdpn_drcl_ciud_res)
      callableStatement.setString(13, fdpn_drcl_tele_res)
      //en caso que el cliente no tenga correo en el core,
      //el correo antiguo debe ser el mismo correo nuevo
      val correoAntiguo: String =
        if (fdpn_dcfd_email_ant == null || fdpn_dcfd_email_ant.isEmpty) fdpn_dcfd_email
        else fdpn_dcfd_email_ant
      callableStatement.setString(14, correoAntiguo)
      callableStatement.setString(15, fdpn_dcfd_email)
      //en caso que el cliente no tenga correo en el core,
      //el tipo correo antiguo debe ser el mismo tipo del correo nuevo
      val tipoCorreoAntiguo: String =
        if (fdpn_dcfd_tipo_ant == null || fdpn_dcfd_tipo_ant.isEmpty) fdpn_dcfd_tipo
        else fdpn_dcfd_tipo_ant
      callableStatement.setString(16, tipoCorreoAntiguo)
      callableStatement.setString(17, fdpn_dcfd_tipo)
      callableStatement.setString(18, fdpn_envio_corresp)
      callableStatement.setString(19, fdpn_telefono_movil_1)
      callableStatement.setString(20, fdpn_pais_tel_mov_1)
      callableStatement.setString(21, fdpn_ocupacion)
      callableStatement.setString(22, fdpn_if_declara_renta)
      callableStatement.setString(23, fdpn_pafd_pais_ant)
      callableStatement.setString(24, fdpn_pafd_pais)
      callableStatement.setString(25, fdpn_ciua)
      callableStatement.setString(26, fdpn_nombre_emp)
      callableStatement.setString(27, fdpn_nit_emp)
      callableStatement.setString(28, fdpn_cargo)
      callableStatement.setString(29, fdpn_dire_emp)
      callableStatement.setString(30, fdpn_ciud_emp)
      callableStatement.setString(31, fdpn_tele_emp)
      callableStatement.setString(32, fdpn_if_vactivos)
      callableStatement.setString(33, fdpn_if_vpasivos)
      callableStatement.setString(34, fdpn_if_vpatrimonio)
      callableStatement.setString(35, fdpn_if_vingresos)
      callableStatement.setString(36, fdpn_if_vegresos)
      callableStatement.setString(37, fdpn_if_vingresos_noop_mes)
      buildResult(connection, callableStatement, 1).get
  }

  //Lista paises
  def listarPaises: Future[String] = transaction {
    connection =>
      connection.setAutoCommit(false)
      val callString = "{ call sf_qportal_web_clientes.Lista_Paises(?,?,?) }"
      val callableStatement = connection prepareCall callString
      callableStatement registerOutParameter (1, OracleTypes.VARCHAR)
      callableStatement registerOutParameter (2, OracleTypes.VARCHAR)
      callableStatement registerOutParameter (3, OracleTypes.CURSOR)
      buildResult(connection, callableStatement, 3).get
  }

  //Lista ciudades
  def listarCiudades(pais: Int): Future[String] = transaction {
    connection =>
      connection.setAutoCommit(false)
      val callString = "{ call sf_qportal_web_clientes.Lista_ciudades(?,?,?,?) }"
      val callableStatement = connection prepareCall callString
      callableStatement registerOutParameter (1, OracleTypes.VARCHAR)
      callableStatement registerOutParameter (2, OracleTypes.VARCHAR)
      callableStatement setString (3, pais.toString)
      callableStatement registerOutParameter (4, OracleTypes.CURSOR)
      buildResult(connection, callableStatement, 4).get
  }

  //Lista tipo correo
  def listarTipoCorreo: Future[String] = transaction {
    connection =>
      connection.setAutoCommit(false)
      val callString = "{ call sf_qportal_web_clientes.Lista_Tipo_Correo(?,?,?) }"
      val callableStatement = connection prepareCall callString
      callableStatement registerOutParameter (1, OracleTypes.VARCHAR)
      callableStatement registerOutParameter (2, OracleTypes.VARCHAR)
      callableStatement registerOutParameter (3, OracleTypes.CURSOR)
      buildResult(connection, callableStatement, 3).get
  }

  //Lista envio correspondencia
  def listarEnvioCorrespondencia: Future[String] = transaction {
    connection =>
      connection.setAutoCommit(false)
      val callString = "{ call sf_qportal_web_clientes.Lista_Envio_Correspondencia(?,?,?) }"
      val callableStatement = connection prepareCall callString
      callableStatement registerOutParameter (1, OracleTypes.VARCHAR)
      callableStatement registerOutParameter (2, OracleTypes.VARCHAR)
      callableStatement registerOutParameter (3, OracleTypes.CURSOR)
      buildResult(connection, callableStatement, 3).get
  }

  //Lista ocupaciones
  def listarOcupaciones: Future[String] = transaction {
    connection =>
      connection.setAutoCommit(false)
      val callString = "{ call sf_qportal_web_clientes.Lista_Ocupaciones(?,?,?) }"
      val callableStatement = connection prepareCall callString
      callableStatement registerOutParameter (1, OracleTypes.VARCHAR)
      callableStatement registerOutParameter (2, OracleTypes.VARCHAR)
      callableStatement registerOutParameter (3, OracleTypes.CURSOR)
      buildResult(connection, callableStatement, 3).get
  }

  //Lista actividades economicas
  def listarActividadesEconomicas: Future[String] = transaction {
    connection =>
      connection.setAutoCommit(false)
      val callString = "{ call sf_qportal_web_clientes.Lista_Actividades_Economicas(?,?,?) }"
      val callableStatement = connection prepareCall callString
      callableStatement registerOutParameter (1, OracleTypes.VARCHAR)
      callableStatement registerOutParameter (2, OracleTypes.VARCHAR)
      callableStatement registerOutParameter (3, OracleTypes.CURSOR)
      buildResult(connection, callableStatement, 3).get
  }

}
