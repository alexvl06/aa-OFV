/**
  * Maneja las versiones de las dependencias de la aplicación.
  *
  * Orden siguiendo el definido en [[Dependencies]].
  */
object Versions {

  val jdk: String                = "1.8"

  val scala: String              = "2.11.8"
  val akka: String               = "2.4.6"
  val spray: String              = "1.3.3"
  val sprayJson: String          = "1.3.2"
  val kafka: String              = "0.8.2.2" // No se puede actualizar mas ya que se re-implementaron los clientes en kafka 0.9
  val scalaIO: String            = "0.4.3"
  val scalaz: String             = "7.2.2"
  val shapeless: String          = "2.1.0" // No se puede actualizar mas ya que versiones superiores no son compatibles con spray-routing-shapeless2
  val slick: String              = "3.1.1"
  val slickPG: String            = "0.9.2" // No se puede actualizar mas, tiene incompatibilidad con slickJodaTime que trae la version de slick_core 0.7.0
  val slickPGJodaTime: String    = "0.10.2" // No se puede actualizar mas ya que obligaría a cambiar a slick 3
  val postgresql: String         = "9.4.1208"  // No se puede actualizar mas ya versiones superiores son para postgres 9.4
  val oracle: String             = "6"
  val c3p0: String               = "0.9.1.2"
  val freeslick: String          = "3.1.1.1"
  val hikariCP: String           = "2.4.6"
  val h2: String                 = "1.4.192"
  val recaptcha4j : String       = "0.0.7"
  val apacheCodec: String        = "1.10"
  val apacheAxis: String         = "1.4"
  val apacheLang: String         = "3.4"
  val commonsDiscovery : String  = "0.2"
  val jacksonDataBind: String    = "2.7.3" // No se puede actualizar mas ya que tiene problema de compatibilidad con jackson-module-scala
  val jacksonModuleScala: String = "2.7.3"
  val playJson: String           = "2.3.10" // No se puede actualizar mas ya que por transitividad trae com.typesafe.config 1.3 que necesita jdk 1.8
  val jasypt: String             = "1.9.2"
  val jaxrpc: String             = "1.1"
  val jsonToken: String          = "1.1"
  val ninbus: String             = "3.10" // Version 4.16.2 , incompatibilidad de código
  val wsdl4j: String             = "1.4"
  val wss4j: String              = "1.5.12" //No se actualiza por clase deprecated de .wssd WSDoAllSender
  val scalate: String            = "1.7.1"
  val scalaLogging: String       = "3.4.0"
  val slf4j: String              = "1.7.21"
  val logback: String            = "1.1.7"

  val junit: String              = "4.12"
  val scalacheck: String         = "1.13.1"
  val scalatest: String          = "2.2.6"
  val specs2: String             = "2.3.13" // No se puede actualizar mas ya que tiene problema de compatibilidad spray 1.3.3
  val restAssured: String        = "2.9.0"
}
