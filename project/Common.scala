import com.timushev.sbt.updates.UpdatesKeys
import com.typesafe.sbt.SbtScalariform.ScalariformKeys
import de.johoop.jacoco4sbt.JacocoPlugin.jacoco
import org.scalastyle.sbt.ScalastylePlugin
import sbt.Keys._
import sbt._

/**
  * Maneja las configuraciones comunes de la aplicación.
  */
object Common {

  val customCompile: TaskKey[Unit] = taskKey[Unit]("customCompile")

  private val commonJavaOptions = Seq(
    "-Xms512m", "-Xmx1G", "-XX:MaxPermSize=256m", "-Djava.awt.headless=true", "-Djava.net.preferIPv4Stack=true", "-XX:+UseCompressedOops",
    "-XX:+UseConcMarkSweepGC", "-XX:+CMSIncrementalMode", "-XX:+PrintGCDetails", "-XX:+PrintGCTimeStamps"
  )

  private val commonJavaOptionsCompile = {
    import Versions._
    Seq("-source", jdk, "-target", jdk, "-Xlint:unchecked", "-deprecation")
  }

  private val commonScalacOptions = Seq(s"-target:jvm-${Versions.jdk}", "-encoding", "UTF-8", "-feature", "-unchecked")

  private val commonScalacOptionsCompile = Seq(
    "-deprecation", // Emit warning and location for usages of deprecated APIs.
    "-feature", // Emit warning and location for usages of features that should be imported explicitly.
    "-unchecked", // Enable additional warnings where generated code depends on assumptions.
    "-Xlint", // Enable recommended additional warnings.
    "-Ywarn-adapted-args", // Warn if an argument list is modified to match the receiver.
    "-Ywarn-dead-code",
    "-Ywarn-value-discard" // Warn when non-Unit expression results are unused.
  )

  /**
    * Maven repositories
    */
  private val commonResolvers = Seq(
    Resolver.sonatypeRepo("snapshots"),
    Resolver.sonatypeRepo("releases"),
    "Spray.io repository" at "http://repo.spray.io",
    "Artifactory Alianza" at "http://artifactory.alianza.com.co:8081/artifactory/third-party-libs/"
  )

  /**
    * Tareas secuenciales ejecutadas al ejecutar compile.
    */
  private val customCompileInit = Def.sequential(
    UpdatesKeys.dependencyUpdates.in(Compile).toTask,
    ScalariformKeys.format.in(Compile).toTask,
    ScalastylePlugin.scalastyle.in(Compile).toTask("")
  )

  /**
    * Configuraciones comunes de los módulos.
    *
    * Orden alfabético.
    */
  val commonSettings: Seq[Setting[_]] = jacoco.settings ++ Seq(
    autoScalaLibrary := false,

    compileOrder in Compile := CompileOrder.JavaThenScala,

    customCompile := customCompileInit.value,

    (compile in Compile) <<= (compile in Compile).dependsOn(customCompile),

    fork in Test := false,

    javaOptions ++= commonJavaOptions,

    javaOptions in Compile ++= commonJavaOptionsCompile,

    organization := "co.com.alianza.portal.transaccional.fiduciaria",

    resolvers ++= commonResolvers,

    scalacOptions ++= commonScalacOptions,

    scalacOptions in Compile ++= commonScalacOptionsCompile,

    scalaVersion := Versions.scala
  )
}
