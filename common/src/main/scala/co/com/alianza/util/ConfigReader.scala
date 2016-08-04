package co.com.alianza.util

import com.typesafe.config.{Config, ConfigFactory}

import scala.collection.JavaConversions._

/**
 * Application configuration reader object
 */
object ConfigReader {

  /**
   * Path for configuration file
   */
  private[ ConfigReader ] val configPath: String = "alianza.comercial.autenticacion."

  /**
   * Configurations file
   */
  private[ ConfigReader ] val conf: Config = ConfigFactory.load

  /**
   * A method that returns full configuration loaded
   * @return Full configuration
   */
  def getConf: Config = conf

  /**
   * A method that returns a double configuration given its property name
   * @param propertyName Property name to look at
   * @return Double configuration
   */
  def readDouble( propertyName: String ): Double = conf.getDouble( configPath + propertyName )

  /**
   * A method that returns an Int configuration given its property name
   * @param propertyName Property name to look at
   * @return Int configuration
   */
  def readInt( propertyName: String ): Int = conf.getInt( configPath + propertyName )

  /**
   * A method that returns an String configuration given its property name
   * @param propertyName Property name to look at
   * @return String configuration
   */
  def readString( propertyName: String ): String = conf.getString( configPath + propertyName )

  /**
   * A method that returns an List of Strings configuration given its property name
   * @param propertyName Property name to look at
   * @return List of Strings configuration
   */
  def readStringList( propertyName: String ): List[ String ] = conf.getStringList( configPath + propertyName ).toList

}
