package co.com.alianza.persistence.conn

import com.typesafe.config.{Config, ConfigFactory}

import com.mchange.v2.c3p0.ComboPooledDataSource
import co.com.alianza.util.ConfigApp
import org.postgresql.ds.PGPoolingDataSource

/**
 * Crea el data Source de la Conexión
 *
 * @author smontanez
 */
object DataBaseAccessAlianza{

	private val config: Config = ConfigApp.conf
	val dataSource: PGPoolingDataSource = createDatasource( ConnectionConfAlianza( config ) )

	private def createDatasource( config: ConnectionConf ): PGPoolingDataSource = {

    val dataSource = new PGPoolingDataSource();
    dataSource.setDataSourceName( config.dataSourceName );
    dataSource.setServerName( config.serverName );
    dataSource.setDatabaseName( config.dataBaseName );
    dataSource.setUser(config.user);
    dataSource.setPassword(config.pass);
    dataSource.setMaxConnections(config.maxPoolSize);
    //new InitialContext().rebind("DataSource", source);
    dataSource
	}
}