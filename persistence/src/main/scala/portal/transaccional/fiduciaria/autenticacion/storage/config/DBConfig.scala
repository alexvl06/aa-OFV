package portal.transaccional.fiduciaria.autenticacion.storage.config

import slick.driver.JdbcProfile
import slick.jdbc.JdbcBackend.DatabaseDef

/**
 * Created by alexa on 22/06/16.
 */
trait DBConfig {

  implicit val driver: JdbcProfile
  implicit val DB: DatabaseDef

}
