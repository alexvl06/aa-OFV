package co.com.alianza.persistence.conn

/**
 * Datos basicos de conexion a base de datos
 *
 * @author seven4n
 */
trait ConnectionConf {

  def dataSourceName: String

  def serverName: String

  def dataBaseName: String

  def user: String

  def pass: String

  def maxPoolSize: Int

}