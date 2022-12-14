package portal.transaccional.fiduciaria.autenticacion.storage.conn.oracle

import com.typesafe.config.Config
import portal.transaccional.fiduciaria.autenticacion.storage.conn.ConnectionConf

/**
 * Obtiene la configuracion de la conexión a base de datos
 *
 * @author smontanez
 */
case class ConnectionConfigCoreAlianza(config: Config) extends ConnectionConf {

  val connectionString = config.getString("db.alianza.core.connectionString")
  val user = config.getString("db.alianza.core.user")
  val pass = config.getString("db.alianza.core.pass")
  val driver = config.getString("db.alianza.core.driver")
  val minPoolSize = config.getInt("db.alianza.core.minPoolSize")
  val acquireIncrement = config.getInt("db.alianza.core.acquireIncrement")
  val maxPoolSize = config.getInt("db.alianza.core.maxPoolSize")
  val checkoutTimeout = config.getInt("db.alianza.core.checkoutTimeout")

  val dataSourceName = config.getString("db.alianza.dataSourceName")
  val serverName = config.getString("db.alianza.serverName")
  val dataBaseName = config.getString("db.alianza.dataBaseName")
}
