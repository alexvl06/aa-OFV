package co.com.alianza.util

import java.io.File
import com.typesafe.config.{Config, ConfigFactory}

/**
 *
 * @author seven4n
 */
object ConfigApp {
  private val classPathConf  = ConfigFactory.load

  private val configFile = new File( classPathConf.getString("alianza.config.path"))
  private val fileConfig = ConfigFactory.parseFile( configFile )
  private val configFileSystem = ConfigFactory.load( fileConfig )

  implicit lazy val conf: Config = configFileSystem.withFallback(classPathConf)

}
