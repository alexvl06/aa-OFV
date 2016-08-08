package portal.transaccional.fiduciaria.autenticacion.storage.config.pg

import freeslick.OracleProfile
import portal.transaccional.fiduciaria.autenticacion.storage.config.DBConfig
import portal.transaccional.fiduciaria.autenticacion.storage.conn.oracle.DataBaseAccesOracleAlianza
import portal.transaccional.fiduciaria.autenticacion.storage.conn.pg.DataBaseAccessPGAlianza
import slick.driver.{ JdbcProfile, PostgresDriver }
import slick.jdbc.JdbcBackend.Database

/**
 * Interface que implementa la configuracion de base de datos para POSTGRES
 */
trait PGConfig extends DBConfig {

  implicit lazy val driver: JdbcProfile = PostgresDriver
  implicit lazy val DB: Database = Database.forDataSource(DataBaseAccessPGAlianza.dataSource)

}

/**
 * Interface que implementa la configuracion de base de datos para ORACLE
 */
trait OracleConfig extends DBConfig {

  implicit lazy val driver: JdbcProfile = OracleProfile
  implicit lazy val DB: Database = Database.forDataSource(DataBaseAccesOracleAlianza.ds)

}
