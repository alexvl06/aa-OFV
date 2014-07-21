package co.com.alianza.persistence.conn.core

import com.typesafe.config.{Config, ConfigFactory}

import oracle.jdbc.xa.client.OracleXADataSource

/**
 * Crea el data Source de la Conexión
 *
 * @author seven4n
 */
object DataBaseAccesCoreAlianza  {

	private val config: Config = ConfigFactory.load
	val ds: OracleXADataSource = createDatasource( ConnectionConfigCoreAlianza( config ) )

	private def createDatasource( config: ConnectionConfigCoreAlianza ): OracleXADataSource = {
    val ds = new OracleXADataSource
    ds.setURL(config.connectionString)
		ds.setUser( config.user )
		ds.setPassword( config.pass )
    //TODO: Configurar Timeout
    ds.setLoginTimeout(config.checkoutTimeout)
    ds
	}
}