package portal.transaccional.fiduciaria.autenticacion.storage.config

import slick.driver.{ H2Driver, JdbcProfile }
import slick.jdbc.JdbcBackend.Database

import scala.concurrent.ExecutionContext

/**
 * Interface que implementa la configuracion de base de datos para H2
 */
trait H2Config extends DBConfig {

  /**
   * Driver para hacer la inyeccion a sclick
   */
  implicit lazy val profile: JdbcProfile = H2Driver

  /**
   * Base de datos para obtener la sesion
   */
  implicit lazy val db: Database = Database.forURL("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", driver = "org.h2.Driver")

  /**
   * Pool de conexiones para los tests de los repositorios y los facades
   */
  implicit lazy val context: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

}