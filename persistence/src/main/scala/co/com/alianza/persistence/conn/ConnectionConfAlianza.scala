package co.com.alianza.persistence.conn

import com.typesafe.config.Config

/**
 * Obtiene la configuracion de la conexión a base de datos
 *
 * @author seven4n
 */
case class ConnectionConfAlianza(config: Config) extends ConnectionConf {

  val dataSourceName = config.getString("db.alianza.dataSourceName")
  val serverName = config.getString("db.alianza.serverName")
  val dataBaseName = config.getString("db.alianza.dataBaseName")
  val user = config.getString("db.alianza.user")
  val pass = config.getString("db.alianza.pass")
  val maxPoolSize = config.getInt("db.alianza.maxPoolSize")

}