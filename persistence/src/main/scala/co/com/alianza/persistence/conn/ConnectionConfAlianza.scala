package co.com.alianza.persistence.conn


import com.typesafe.config.Config

/**
 * Obtiene la configuracion de la conexión a base de datos
 *
 * @author seven4n
 */
case class ConnectionConfAlianza( config: Config ) extends ConnectionConf {

	val connectionString = config.getString( "db.alianza.connectionString" )
	val user = config.getString( "db.alianza.user" )
	val pass = config.getString( "db.alianza.pass" )
	val driver = config.getString( "db.alianza.driver" )
	val minPoolSize = config.getInt( "db.alianza.minPoolSize" )
	val acquireIncrement = config.getInt( "db.alianza.acquireIncrement" )
	val maxPoolSize = config.getInt( "db.alianza.maxPoolSize" )
  val checkoutTimeout = config.getInt( "db.alianza.checkoutTimeout" )
}