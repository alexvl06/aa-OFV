package co.com.alianza.persistence.conn.oracle

import javax.sql.DataSource

import co.com.alianza.util.ConfigApp
import com.typesafe.config.Config
import com.zaxxer.hikari.HikariDataSource

/**
 * Crea el data Source de la Conexión
 *
 * @author seven4n
 */
object DataBaseAccessOracleAlianza {

  implicit private val config: Config = ConfigApp.conf
  val ds: DataSource = createDatasource(ConnectionConfigCoreAlianza(config))

  private def createDatasource(config: ConnectionConfigCoreAlianza): DataSource = {
    import config._
    val ds = new HikariDataSource()
    ds.setJdbcUrl(connectionString)
    ds.setUsername(user)
    ds.setPassword(pass)
    ds.setLoginTimeout(checkoutTimeout)
    ds.addDataSourceProperty("driverType", "thin")
    ds
  }

}
