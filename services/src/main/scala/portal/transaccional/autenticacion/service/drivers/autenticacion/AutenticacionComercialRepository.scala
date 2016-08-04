package portal.transaccional.autenticacion.service.drivers.autenticacion

import slick.driver.JdbcProfile
import slick.jdbc.JdbcBackend._

import scala.concurrent.{ ExecutionContext, Future }
import scalaz.Validation

trait AutenticacionComercialRepository {

  def authenticateLDAP(userType: Int, username: String, password: String, ip: String): Future[String]

  def authenticateAdmin(username: String, password: String, ip: String): Future[String]
}
