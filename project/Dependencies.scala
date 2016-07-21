import sbt._

/**
 * Maneja las dependencias de la aplicación.
 */
object Dependencies {

  /**
   * Define exclusiones necesarias en las librerías para evitar conflictos.
   *
   * Hay dos tipos:
   * - Exclusions: que son paquetes de exclusiones usado por solo una librería.
   * - Exclude: que una sola exclusión especifica que puede ser usada por una o varias librerías.
   *
   * Orden:
   * - Primero Exclusions luego Exclude.
   * - Alfabético entre Exclusions.
   * - Por composición entre Exclude.
   */
  private[Dependencies] implicit class Exclude(module: ModuleID) {

    def kafkaExclusions: ModuleID = {
      module.logScalaExclude.excludeAll(
        ExclusionRule("com.sun.jmx", "jmxri"),
        ExclusionRule("com.sun.jdmk", "jmxtools"),
        // Hay un problema de compatibilidad binaria con akka-remote_2.11 que no fue posible resolver de la forma correcta haciendo upgrade o downgrade
        ExclusionRule("io.netty", "netty"),
        ExclusionRule("javax.jms", "jms"),
        ExclusionRule("jline", "jline")
      )
    }

    // Hay un problema de compatibilidad binaria con play-json que que no fue posible resolver de la forma correcta haciendo upgrade o downgrade
    def jodaTimeExclude: ModuleID = module.logScalaExclude.exclude("joda-time", "joda-time")

    // Hay un problema de compatibilidad binaria con spray-testkit que que no fue posible resolver de la forma correcta haciendo upgrade o downgrade
    def specs2Exclude: ModuleID = module.logScalaExclude.exclude("org.specs2", "specs2_2.11")

    def logScalaExclude: ModuleID = module.logbackExclude.scalaLibraryExclude

    def logbackExclude: ModuleID = {
      module.log4jExclude.excludeAll(
        ExclusionRule("ch.qos.logback", "logback-classic"),
        ExclusionRule("ch.qos.logback", "logback-core")
      )
    }

    def log4jExclude: ModuleID = {
      module.excludeAll(
        ExclusionRule("commons-logging", "commons-logging"),
        ExclusionRule("log4j", "log4j"),
        ExclusionRule("org.slf4j", "jcl-over-slf4j"),
        ExclusionRule("org.slf4j", "jul-to-slf4j"),
        ExclusionRule("org.slf4j", "log4j-over-slf4j"),
        ExclusionRule("org.slf4j", "slf4j-api"),
        ExclusionRule("org.slf4j", "slf4j-jcl"),
        ExclusionRule("org.slf4j", "slf4j-jdk14"),
        ExclusionRule("org.slf4j", "slf4j-log4j12"),
        ExclusionRule("org.slf4j", "slf4j-nop"),
        ExclusionRule("org.slf4j", "slf4j-simple")
      )
    }

    def scalaLibraryExclude: ModuleID = module.exclude("org.scala-lang", "scala-library")
  }

  /**
   * Define las librerías necesarias para compilar.
   *
   * Orden por importancia y prioridad, primero cosas como scala, akka y finalmente utilidades y log.
   */
  private[Dependencies] object CompileDep {

    import Versions._

    val scalaCompiler         = "org.scala-lang"                 % "scala-compiler"           % scala logScalaExclude
    val scalaReflect          = "org.scala-lang"                 % "scala-reflect"            % scala logScalaExclude
    val scalaLibrary          = "org.scala-lang"                 % "scala-library"            % scala logbackExclude

    val akkaActor             = "com.typesafe.akka"             %% "akka-actor"               % akka logScalaExclude
    val akkaSlf4j             = "com.typesafe.akka"             %% "akka-slf4j"               % akka logScalaExclude
    val akkaClusterLib        = "com.typesafe.akka"             %% "akka-cluster"             % akka logScalaExclude

    val sprayCan              = "io.spray"                      %% "spray-can"                % spray logScalaExclude
    val sprayRouting          = "io.spray"                      %% "spray-routing-shapeless2" % spray logScalaExclude
    val sprayClient           = "io.spray"                      %% "spray-client"             % spray logScalaExclude
    val sprayHttp             = "io.spray"                      %% "spray-http"               % spray logScalaExclude
    val sprayHttpx            = "io.spray"                      %% "spray-httpx"              % spray logScalaExclude
    val sprayJsonLib          = "io.spray"                      %% "spray-json"               % sprayJson logScalaExclude
    val sprayCaching          = "io.spray"                      %% "spray-caching"            % spray logScalaExclude

    val kafkaLib              = "org.apache.kafka"              %% "kafka"                    % kafka kafkaExclusions

    val scalaIOCore           = "com.github.scala-incubator.io" %% "scala-io-core"            % scalaIO logScalaExclude
    val scalaIOFile           = "com.github.scala-incubator.io" %% "scala-io-file"            % scalaIO logScalaExclude
    val scalazLib             = "org.scalaz"                    %% "scalaz-core"              % scalaz logScalaExclude
    val shapelessLib          = "com.chuusai"                   %% "shapeless"                % shapeless logScalaExclude

    val slickLib              = "com.typesafe.slick"            %% "slick"                    % slick logScalaExclude
    val slickPGLib            = "com.github.tminglei"           %% "slick-pg"                 % slickPG logScalaExclude
    val slickPGJodaTimeLib    = "com.github.tminglei"           %% "slick-pg_joda-time"       % slickPGJodaTime logScalaExclude

    //val c3p0Lib               = "c3p0"                           % "c3p0"                     % c3p0 logScalaExclude
    val postgresqlLib         = "org.postgresql"                 % "postgresql"               % postgresql logScalaExclude
    val oracleLib             = "oracle"                         % "ojdbc"                    % oracle logScalaExclude
    val h2Lib                 = "com.h2database"                 % "h2"                       % h2 logScalaExclude
    val freeslickLib          = "org.suecarter"                 %% "freeslick"                % freeslick
    val hikariCPLib           = "com.zaxxer"                     % "HikariCP"                 % hikariCP logScalaExclude

    val recaptcha4jLib        = "net.tanesha.recaptcha4j"        % "recaptcha4j"              % recaptcha4j logScalaExclude

    val commonsLang3Lib       = "org.apache.commons"             % "commons-lang3"            % apacheLang logScalaExclude
    val axisLib               = "org.apache.axis"                % "axis"                     % apacheAxis logScalaExclude
    val wss4jLib              = "org.apache.ws.security"         % "wss4j"                    % wss4j jodaTimeExclude
    val commonsCodecLib       = "commons-codec"                  % "commons-codec"            % apacheCodec logScalaExclude
    val discoveryLib          = "commons-discovery"              % "commons-discovery"        % commonsDiscovery
    val wsdl4jLib             = "wsdl4j"                         % "wsdl4j"                   % wsdl4j logScalaExclude
    val jaxrpcLib             = "javax.xml"                      % "jaxrpc-api"               % jaxrpc logScalaExclude
    val playJsonLib           = "com.typesafe.play"             %% "play-json"                % playJson logScalaExclude
    val jacksonDatabindLib    = "com.fasterxml.jackson.core"     % "jackson-databind"         % jacksonDataBind logScalaExclude
    val jacksonModuleScalaLib = "com.fasterxml.jackson.module"  %% "jackson-module-scala"     % jacksonModuleScala logScalaExclude
    val jsonTokenLib          = "com.googlecode.jsontoken"       % "jsontoken"                % jsonToken jodaTimeExclude
    val ninbusLib             = "com.nimbusds"                   % "nimbus-jose-jwt"          % ninbus logScalaExclude
    val jasyptLib             = "org.jasypt"                     % "jasypt"                   % jasypt logScalaExclude
    val scalateLib            = "org.scalatra.scalate"          %% "scalate-core"             % scalate logScalaExclude

    val scalaLoggingLib       = "com.typesafe.scala-logging"    %% "scala-logging"            % scalaLogging logScalaExclude
    val slf4jApi              = "org.slf4j"                      % "slf4j-api"                % slf4j
    val jclOverSlf4j          = "org.slf4j"                      % "jcl-over-slf4j"           % slf4j
    val log4jOverSlf4j        = "org.slf4j"                      % "log4j-over-slf4j"         % slf4j
    val julToSlf4j            = "org.slf4j"                      % "jul-to-slf4j"             % slf4j
    val logbackClassic        = "ch.qos.logback"                 % "logback-classic"          % logback log4jExclude
    val logbackCore           = "ch.qos.logback"                 % "logback-core"             % logback log4jExclude
  }

  /**
   * Define las librerías necesarias para pruebas.
   *
   * Orden alfabético.
   */
  private[Dependencies] object TestDep {

    import Versions._

    val akkaTestkit     = "com.typesafe.akka"      %% "akka-testkit"  % akka % Test logScalaExclude
    val junitLib        = "junit"                   % "junit"         % junit % Test logScalaExclude
    val restAssuredLib  = "com.jayway.restassured"  % "rest-assured"  % restAssured % Test logScalaExclude
    val scalatestLib    = "org.scalatest"          %% "scalatest"     % scalatest % Test logScalaExclude
    val scalacheckLib   = "org.scalacheck"         %% "scalacheck"    % scalacheck % Test logScalaExclude
    val sprayTestkitLib = "io.spray"               %% "spray-testkit" % spray % Test specs2Exclude
    val specs2Lib       = "org.specs2"             %% "specs2"        % specs2 % Test logScalaExclude
  }

  import Dependencies.CompileDep._
  import Dependencies.TestDep._

  val scalaLibs: Seq[ModuleID]      = Seq(scalaCompiler, scalaReflect, scalaLibrary)
  val akkaLibs: Seq[ModuleID]       = Seq(akkaActor, akkaClusterLib, akkaSlf4j, logbackClassic)
  val sprayLibs: Seq[ModuleID]      = Seq(sprayCan, sprayCaching, sprayRouting, sprayClient, sprayHttp, sprayHttpx, sprayJsonLib)
  val kafkaLibs: Seq[ModuleID]      = Seq(kafkaLib)
  val functionalLibs: Seq[ModuleID] = Seq(scalaIOCore, scalaIOFile, scalazLib, shapelessLib)
  val slickLibs: Seq[ModuleID]      = Seq(slickLib, slickPGLib, slickPGJodaTimeLib)
  val dbLibs: Seq[ModuleID]         = Seq(postgresqlLib, oracleLib, h2Lib, freeslickLib, hikariCPLib)

  val utilLibs: Seq[ModuleID]       = Seq(
    commonsLang3Lib, commonsCodecLib, discoveryLib, playJsonLib, wsdl4jLib, jacksonDatabindLib, jacksonModuleScalaLib, jasyptLib, scalateLib, axisLib, jaxrpcLib, wss4jLib, ninbusLib, jsonTokenLib
  )

  val recaptchaLibs: Seq[ModuleID]  = Seq(recaptcha4jLib)
  val loggingLibs: Seq[ModuleID]    = Seq(scalaLoggingLib, slf4jApi, jclOverSlf4j, log4jOverSlf4j, julToSlf4j, logbackClassic, logbackCore)
  val testLibs: Seq[ModuleID]       = Seq(akkaTestkit, sprayTestkitLib, scalatestLib, junitLib, restAssuredLib, scalacheckLib, specs2Lib)
}
