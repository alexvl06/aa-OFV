
import Dependencies._
import Versions._
import com.timushev.sbt.updates.UpdatesKeys
import com.typesafe.sbt.SbtScalariform.ScalariformKeys
import de.johoop.jacoco4sbt.JacocoPlugin.jacoco
import org.scalastyle.sbt.ScalastylePlugin
import sbt.Keys._
import sbt._
import spray.revolver.RevolverPlugin.Revolver

object Common {
  val compileSbtUpdates: TaskKey[Unit] = taskKey[Unit]("compileSbtUpdates")
  val compileScalariform: TaskKey[Seq[File]] = taskKey[Seq[File]]("compileScalariform")
  val compileScalastyle: TaskKey[Unit] = taskKey[Unit]("compileScalastyle")

  val commonSettings: Seq[Setting[_]] = jacoco.settings ++ Seq(

    organization := "co.com.alianza.portal.transaccional.autenticacion-autorizacion",

    scalaVersion := commonScalaVersion,

    autoScalaLibrary := false,

    resolvers ++= commonResolvers,

    scalacOptions ++= commonScalacOptions,

    compileOrder in Compile := CompileOrder.JavaThenScala,

    libraryDependencies ++= commonLibraries ++ reactiveLibraries,

    mainClass in Revolver.reStart := Some( "co.com.alianza.app.Boot" ),

    baseDirectory in Revolver.reStart := file("./")

  ) ++ compileTasks


  //mainClass in Revolver.reStart := Some( "co.com.alianza.app.Boot" )

  //baseDirectory in Revolver.reStart := file("./")

  //EclipseKeys.createSrc := EclipseCreateSrc.Default + EclipseCreateSrc.Resource

  //EclipseKeys.projectFlavor := EclipseProjectFlavor.Scala

  /**
    * scalac arguments
    **/
  private def commonScalacOptions = Seq("-unchecked", "-deprecation", "-Xlint", "-Ywarn-dead-code", "-language:_", "-target:jvm-1.7", "-encoding", "UTF-8")

  /**
    * Maven repositories
    **/

  /**
    * Maven repositories
    **/
  def commonResolvers = Seq(
    Resolver.sonatypeRepo("snapshots"),
    Resolver.sonatypeRepo("releases"),
    Resolver.typesafeRepo("releases"),
    "Sonatype Snapshots"  at "http://oss.sonatype.org/content/repositories/snapshots",
    "Sonatype Releases"   at "http://oss.sonatype.org/content/repositories/releases",
    "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/",
    "Spray.io repository" at "http://repo.spray.io",
    "artifactory alianza" at "http://artifactory.alianza.com.co:8081/artifactory/third-party-libs/",
    "maven repository"    at "http://repo1.maven.org/maven2/"
  )


  private def compileTasks: Seq[Setting[_]] = Seq(
    compileSbtUpdates := UpdatesKeys.dependencyUpdates.in(Compile).toTask.value,
    compileScalariform := ScalariformKeys.format.in(Compile).toTask.value,
    compileScalastyle := ScalastylePlugin.scalastyle.in(Compile).toTask("").value,
    compileScalariform <<= compileScalariform dependsOn compileSbtUpdates,
    compileScalastyle <<= compileScalastyle dependsOn compileScalariform,
    (compile in Compile) <<= (compile in Compile) dependsOn compileScalastyle
  )
}
