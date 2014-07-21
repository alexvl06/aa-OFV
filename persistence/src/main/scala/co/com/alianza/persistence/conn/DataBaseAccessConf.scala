package co.com.alianza.persistence.conn


/**
 * Datos basicos de conexion a base de datos
 *
 * @author seven4n
 */
trait ConnectionConf {

	def connectionString: String

	def user: String

	def pass: String

	def driver: String

	def minPoolSize: Int

	def acquireIncrement: Int

	def maxPoolSize: Int

  def checkoutTimeout:Int
}