package portal.transaccional.fiduciaria.autenticacion.storage.conn.oracle

import com.typesafe.config.{ Config, ConfigFactory }

import oracle.jdbc.xa.client.OracleXADataSource
import co.com.alianza.util.ConfigApp

/**
 * Crea el data Source de la Conexión
 *
 * @author seven4n
 */
object DataBaseAccesOracleAlianza {

  private val config: Config = ConfigApp.conf
  val ds: OracleXADataSource = createDatasource(ConnectionConfigCoreAlianza(config))

  private def createDatasource(config: ConnectionConfigCoreAlianza): OracleXADataSource = {
    val ds = new OracleXADataSource
    ds.setURL(config.connectionString)
    ds.setUser(config.user)
    ds.setPassword(config.pass)
    ds.setLoginTimeout(config.checkoutTimeout)
    ds
  }
}