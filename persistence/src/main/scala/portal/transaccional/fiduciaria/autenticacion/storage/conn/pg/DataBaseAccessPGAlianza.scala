package portal.transaccional.fiduciaria.autenticacion.storage.conn.pg

import co.com.alianza.util.ConfigApp
import com.typesafe.config.Config
import org.postgresql.ds.PGPoolingDataSource
import portal.transaccional.fiduciaria.autenticacion.storage.conn.ConnectionConf

/**
 * Crea el data Source de la Conexión
 *
 * @author smontanez
 */
object DataBaseAccessPGAlianza {

  private val config: Config = ConfigApp.conf
  val dataSource: PGPoolingDataSource = createDatasource(ConnectionConfAlianza(config))

  private def createDatasource(config: ConnectionConf): PGPoolingDataSource = {

    val dataSource = new PGPoolingDataSource()
    dataSource.setDataSourceName(config.dataSourceName)
    dataSource.setServerName(config.serverName)
    dataSource.setDatabaseName(config.dataBaseName)
    dataSource.setUser(config.user)
    dataSource.setPassword(config.pass)
    dataSource.setMaxConnections(config.maxPoolSize)
    dataSource
  }
}