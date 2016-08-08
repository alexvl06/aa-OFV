package portal.transaccional.fiduciaria.autenticacion.storage.conn

import com.typesafe.config.Config

/**
 * Datos basicos de conexion a base de datos
 *
 * @author seven4n
 */
trait ConnectionConf {

  def user: String

  def pass: String

  def dataSourceName: String

  def serverName: String

  def dataBaseName: String

  def maxPoolSize: Int

}
