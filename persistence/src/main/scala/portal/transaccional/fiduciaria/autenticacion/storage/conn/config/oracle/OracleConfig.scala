package co.com.alianza.persistence.config.oracle

import co.com.alianza.persistence.config.DBConfig
import com.typesafe.config.ConfigFactory
import portal.transaccional.fiduciaria.autenticacion.storage.conn.oracle.DataBaseAccessOracleAlianza
import freeslick.OracleProfile
import slick.jdbc.JdbcBackend.Database

/**
 * Interface que implementa la configuracion de base de datos para ORACLE
 */
trait OracleConfig extends DBConfig {

  implicit lazy val profile: OracleProfile = OracleProfile
  implicit lazy val db: Database = Database.forDataSource(DataBaseAccessOracleAlianza.ds)
  //implicit lazy val db: Database = Database.forConfig("dbs.alianza", ConfigFactory.load())

}
