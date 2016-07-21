package co.com.alianza.persistence.config.oracle

import co.com.alianza.persistence.config.DBConfig
import co.com.alianza.persistence.conn.oracle.DataBaseAccessOracleAlianza
import freeslick.OracleProfile
import slick.jdbc.JdbcBackend.Database

/**
 * Interface que implementa la configuracion de base de datos para ORACLE
 */
trait OracleConfig extends DBConfig {

  implicit lazy val driver: OracleProfile = OracleProfile
  implicit lazy val DB: Database = Database.forDataSource(DataBaseAccessOracleAlianza.ds)

}
