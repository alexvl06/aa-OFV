package portal.transaccional.fiduciaria.autenticacion.storage.conn

/**
 * Datos basicos de conexion a base de datos
 *
 * @author seven4n
 */
trait ConnectionConf {

  def connectionString: String

  def user: String

  def pass: String

  def checkoutTimeout: Int

  def dataSourceName: String

  def serverName: String

  def dataBaseName: String

  def maxPoolSize: Int

}
