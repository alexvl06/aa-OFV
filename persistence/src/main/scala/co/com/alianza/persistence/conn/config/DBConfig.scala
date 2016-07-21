package co.com.alianza.persistence.config

import slick.driver.JdbcProfile
import slick.jdbc.JdbcBackend.DatabaseDef

/**
 * Created by alexa on 22/06/16.
 */
trait DBConfig {

  implicit val profile: JdbcProfile
  implicit val db: DatabaseDef

}
