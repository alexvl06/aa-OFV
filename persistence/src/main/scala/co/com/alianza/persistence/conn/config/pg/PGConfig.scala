package co.com.alianza.persistence.config.pg

import co.com.alianza.persistence.config.DBConfig
import co.com.alianza.persistence.conn.pg.DataBaseAccessPGAlianza
import slick.driver.{ JdbcProfile, PostgresDriver }
import slick.jdbc.JdbcBackend._

/**
 * Created by s4npr02 on 22/06/16.
 */
trait PGConfig extends DBConfig {

  implicit lazy val profile: JdbcProfile = PostgresDriver
  implicit lazy val db: Database = Database.forDataSource(DataBaseAccessPGAlianza.dataSource)

}
