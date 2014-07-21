import AssemblyKeys._
import Common._

assemblySettings

mergeStrategy in assembly <<= (mergeStrategy in assembly) { (old) =>
{
  case "reference.conf" => MergeStrategy.concat
  case PathList("META-INF", xs @ _*) =>
    (xs map {_.toLowerCase}) match {
      case _ => MergeStrategy.discard
    }
  case x => MergeStrategy.first
}
}

test in assembly := {}

outputPath in assembly := file( "../alianza-aa-persistence.jar" )

name := "autenticacion-autorizacion-persistence"

organization := "co.s4n"

version := "0.1-SNAPSHOT"

scalaVersion := commonScalaVersion

scalacOptions ++= commonScalacOptions

unmanagedSourceDirectories in Compile := (scalaSource in Compile).value :: Nil

unmanagedSourceDirectories in Test := (scalaSource  in Test).value :: Nil

EclipseKeys.createSrc := EclipseCreateSrc.Default + EclipseCreateSrc.Resource

EclipseKeys.projectFlavor := EclipseProjectFlavor.Scala

resolvers ++= commonResolvers

libraryDependencies ++= commonLibraries ++ reactiveLibraries ++ Seq("com.typesafe.slick" %% "slick" % "2.0.2", "postgresql" % "postgresql" % "9.1-901.jdbc4", "c3p0" % "c3p0" % "0.9.1.2", "com.github.tminglei" % "slick-pg_2.10" % "0.6.0-M1", "com.github.tminglei" % "slick-pg_joda-time_2.10" % "0.6.0-M1")

seq( Revolver.settings: _* )
