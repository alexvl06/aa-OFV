package co.com.alianza.util

import java.io.{ FileInputStream, File }
import com.typesafe.config.{ Config, ConfigFactory }
import java.util.Properties
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor
import org.jasypt.properties.EncryptableProperties
import java.net.{ NetworkInterface, InetAddress }

/**
 *
 * @author seven4n
 */
object ConfigApp {
  private val classPathConf = ConfigFactory.load

  private val encryptor = new StandardPBEStringEncryptor()
  val network = NetworkInterface.getByName("enp2s0")
  val mac = (network.getHardwareAddress() map { "%02x" format _ } mkString "-")
  encryptor.setPassword(mac.toUpperCase + "4l14nz4_p4ss_k3y")

  private val cryptoProps = new EncryptableProperties(encryptor)
  cryptoProps.load(new FileInputStream(classPathConf.getString("alianza.config.path")))

  val prop = new Properties()

  val nombresPropiedades = cryptoProps.stringPropertyNames().toArray

  for (nombrePropiedad <- nombresPropiedades) prop.setProperty(nombrePropiedad.toString, cryptoProps.getProperty(nombrePropiedad.toString))

  private val configFileSystem = ConfigFactory.parseProperties(prop)

  implicit lazy val conf: Config = configFileSystem.withFallback(classPathConf)

}
