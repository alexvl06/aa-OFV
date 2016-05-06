/**
  * Maneja las versiones de las dependencias de la aplicación.
  *
  * Orden siguiendo el definido en [[Dependencies]].
  */

object Versions {

  val commonScalaVersion: String         = "2.11.8"
  val akka: String                = "2.3.15" // Akka 2.4.4 necesita de java 8 (java.util.concurrent)
  val akkaLogback: String         = "1.1.7"
  val akkaCluster: String         = "2.3.15"
  val kafka: String               = "0.9.0.1"
  val apacheCodec: String         = "1.10"
  val apacheAxis: String          = "1.4"
  val apacheio: String            = "2.4"
  val apacheLang: String          = "3.4"
  val apacheLogin: String         = "1.1.3"
  val scalacheck: String          = "1.13.1"
  val scalaio: String             = "0.4.3"
  val scalatest: String           = "2.2.6"
  val scalaz: String              = "7.1.8" // No se puede actualizar a 7.2.2 por falla con flatmap
  val shapeless: String           = "2.1.0" // La 2.3.0 incompatilidad con spray-routing que trae la versión 2.1.0
  val commonsDiscovery : String   = "0.2"
  val junit: String               = "4.10"
  val jacksonDataBind: String     = "2.7.4"
  val jacksonModuleScala: String  = "2.7.3"
  val jasypt: String              = "1.9.2"
  val jasperreports: String       = "5.6.0"
  val japerreportsFonts: String   = "4.0.0"
  val javaxMail: String           = "1.4.5"
  val jaxrpc: String              = "1.1"
  val jdkVersion: String          = "1.7"
  val jsch: String                = "0.1.51"
  val jsonToken: String           = "1.1"
  val ninbus: String              = "3.10" // Version 4.16.2 , incompatibilidad de código
  val specs2: String              = "2.3.13"
  val sprayTestkit: String        = "1.3.3"
  val spray: String               = "1.3.3"
  val sprayJson: String           = "1.3.2"
  val playJson: String            = "2.4.0-M2" // Version superiores trae play iteratees y genera conflicto con typesafe.config 1.3.0
  val restAssured: String         = "1.8.1"
  val ojdbc: String               = "6"
  val wsdl4j: String              = "1.4"
  val wss4j: String               = "1.6.19"
  val scalate: String             = "1.6.1"

  //subProject Persistence
  val c3p0: String                = "0.9.1.2"
  val postgreSql: String          = "9.1-901.jdbc4"
  val slick: String               = "2.1.0"
  val slickpg: String             = "0.6.3"   // Version 0.7.0 - incompatibilidad con slickJodaTime que trae la version de slick_core 0.7.0
  val slickpgJodaTime: String     = "0.6.3"  // Version 0.10.0 , incompatibilidad con código

  //subproject Service
  val recaptcha4j : String        = "0.0.7"

  //Evicted de Jodatime se deja, debido a que JsonToken ni PlayJson pueden actualizarse, y slickPG-jodaTime en su versión
  //mas baja sigue utilizando jodatime version 2.3
}
