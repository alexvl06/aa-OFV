import com.timushev.sbt.updates.UpdatesKeys
import com.typesafe.sbt.SbtScalariform.ScalariformKeys
import de.johoop.jacoco4sbt.JacocoPlugin.jacoco
import org.scalastyle.sbt.ScalastylePlugin
import sbt.Keys._
import sbt._
import Versions._

object Common {
  /**
   * Tareas secuenciales ejecutadas al correr compile.
   */
  val customCompile: TaskKey[Unit] = taskKey[Unit]("customCompile")

  private val commonResolvers = Seq(
    Resolver.sonatypeRepo("snapshots"),
    Resolver.sonatypeRepo("releases"),
    Resolver.typesafeRepo("releases"),
    "Spray.io repository" at "http://repo.spray.io",
    "artifactory alianza" at "http://artifactory.alianza.com.co:8081/artifactory/third-party-libs/",
    "maven repository"    at "http://repo1.maven.org/maven2/"
  )

  private val commonJavaOptions = Seq(
    "-Xms512m", "-Xmx1G", "-Djava.awt.headless=true", "-Djava.net.preferIPv4Stack=true", "-XX:+UseCompressedOops", "-XX:+UseConcMarkSweepGC",
    "-XX:+CMSIncrementalMode", "-XX:+PrintGCDetails", "-XX:+PrintGCTimeStamps", "-feature"
  )

  private val commonJavaOptionsCompile = {
    import Versions._
    Seq("-source", jdkVersion, "-target", jdkVersion, "-Xlint:unchecked", "-Xlint:deprecation", "-feature")
  }

  private val commonScalacOptions = Seq("-unchecked", "-deprecation", "-Xlint", "-Ywarn-dead-code", "-language:_", "-target:jvm-1.7", "-encoding", "UTF-8")

  private val customCompileInit = Def.sequential(
    UpdatesKeys.dependencyUpdates.in(Compile).toTask,
    ScalariformKeys.format.in(Compile).toTask,
    ScalastylePlugin.scalastyle.in(Compile).toTask("")
  )


  val commonSettings: Seq[Setting[_]] = jacoco.settings ++ Seq(
    autoScalaLibrary := false,

    compileOrder in Compile := CompileOrder.JavaThenScala,

    customCompile := customCompileInit.value,

    (compile in Compile) <<= (compile in Compile).dependsOn(customCompile),

    fork := true,

    javaOptions ++= commonJavaOptions,

    javaOptions in Compile ++= commonJavaOptionsCompile,

    organization := "co.com.alianza.portal.transaccional.autenticacion-autorizacion",

    resolvers ++= commonResolvers,

    scalacOptions ++= commonScalacOptions,

    scalaVersion := commonScalaVersion
  )
}
