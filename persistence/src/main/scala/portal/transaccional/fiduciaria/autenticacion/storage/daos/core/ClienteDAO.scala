package portal.transaccional.fiduciaria.autenticacion.storage.daos.core

import oracle.jdbc.OracleTypes
import portal.transaccional.fiduciaria.autenticacion.storage.config.DBConfig
import portal.transaccional.fiduciaria.autenticacion.storage.helpers.AlianzaStorageHelper

import scala.concurrent.{ ExecutionContext, Future }

/**
 * Created by alexandra on 25/07/16.
 */
case class ClienteDAO()(implicit val ec: ExecutionContext, dcConfig: DBConfig) extends AlianzaStorageHelper {

  def consultaCliente(numDocumento: String): Future[String] = transaction {
    connection =>
      connection.setAutoCommit(false)
      val callString = "{ call sf_qportal_web.Cliente(?,?,?,?) }"
      val callableStatement = connection prepareCall callString
      callableStatement registerOutParameter (1, OracleTypes.VARCHAR)
      callableStatement registerOutParameter (2, OracleTypes.VARCHAR)
      callableStatement.setString(3, numDocumento)
      callableStatement registerOutParameter (4, OracleTypes.CURSOR)
      buildResult(connection, callableStatement, 4).get
  }

  def consultaGrupo(idGrupo: String): Future[String] = transaction {
    connection =>
      connection.setAutoCommit(false)
      val callString = "{ call sf_qportal_web.validar_grupo_cliente(?,?,?,?) }"
      val callableStatement = connection prepareCall callString
      callableStatement registerOutParameter (1, OracleTypes.VARCHAR)
      callableStatement registerOutParameter (2, OracleTypes.VARCHAR)
      callableStatement.setString(3, idGrupo)
      callableStatement registerOutParameter (4, OracleTypes.CURSOR)
      buildResult(connection, callableStatement, 4).get
  }

  //  def consultarFideicomisosInmobiliarios(numDocumento: String): Future[Boolean] = {
  //    if (numDocumento == "860000185") Future.successful(true) else Future.successful(false)
  //  }

}