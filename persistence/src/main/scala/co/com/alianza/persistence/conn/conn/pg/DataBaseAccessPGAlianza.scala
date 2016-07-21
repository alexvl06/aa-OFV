package co.com.alianza.persistence.conn.pg

import javax.sql.DataSource

import co.com.alianza.util.ConfigApp
import com.typesafe.config.Config
import com.zaxxer.hikari.HikariDataSource

/**
 * Crea el data Source de la Conexión
 *
 * @author smontanez
 */
object DataBaseAccessPGAlianza {

  private val config: Config = ConfigApp.conf

  val dataSource: DataSource = createDatasource(ConnectionConfAlianza(config))

  private def createDatasource(config: ConnectionConfAlianza): DataSource = {

    val ds = new HikariDataSource()
    ds.setDataSourceClassName("org.postgresql.ds.PGPoolingDataSource")
    ds.addDataSourceProperty("dataSourceName", config.dataSourceName)
    ds.addDataSourceProperty("serverName", config.serverName)
    ds.addDataSourceProperty("databaseName", config.dataBaseName)
    ds.addDataSourceProperty("user", config.user)
    ds.addDataSourceProperty("password", config.pass)
    ds
  }

}
