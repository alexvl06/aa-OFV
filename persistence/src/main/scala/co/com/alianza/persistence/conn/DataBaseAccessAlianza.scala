package co.com.alianza.persistence.conn

import com.typesafe.config.{Config, ConfigFactory}

import com.mchange.v2.c3p0.ComboPooledDataSource

/**
 * Crea el data Source de la Conexión
 *
 * @author smontanez
 */
object DataBaseAccessAlianza{

	private val config: Config = ConfigFactory.load
	val dataSource: ComboPooledDataSource = createDatasource( ConnectionConfAlianza( config ) )

	private def createDatasource( config: ConnectionConf ): ComboPooledDataSource = {
		val dataSource = new ComboPooledDataSource
    dataSource.setDriverClass( config.driver )
    dataSource.setJdbcUrl( config.connectionString )
    dataSource.setMinPoolSize( config.minPoolSize )
    dataSource.setAcquireIncrement( config.acquireIncrement )
    dataSource.setMaxPoolSize( config.maxPoolSize )
    dataSource.setUser( config.user )
    dataSource.setPassword( config.pass )
    dataSource.setCheckoutTimeout(config.checkoutTimeout)
    dataSource
	}
}