import Common._

organization := "co.s4n"

version := "0.1-SNAPSHOT"

scalaVersion := commonScalaVersion

scalacOptions ++= commonScalacOptions

unmanagedSourceDirectories in Compile := (scalaSource in Compile).value :: Nil

unmanagedSourceDirectories in Test := (scalaSource  in Test).value :: Nil

EclipseKeys.createSrc := EclipseCreateSrc.Default + EclipseCreateSrc.Resource

EclipseKeys.projectFlavor := EclipseProjectFlavor.Scala

resolvers ++= commonResolvers

def postgresqljdbc = Seq(
  "org.postgresql" % "postgresql" % "9.3-1102-jdbc41"
)

libraryDependencies ++= commonLibraries ++ reactiveLibraries ++ postgresqljdbc ++ Seq("com.typesafe.slick" %% "slick" % "2.0.2", "postgresql" % "postgresql" % "9.1-901.jdbc4", "c3p0" % "c3p0" % "0.9.1.2", "com.github.tminglei" % "slick-pg_2.10" % "0.6.0-M1", "com.github.tminglei" % "slick-pg_joda-time_2.10" % "0.6.0-M1")

seq( Revolver.settings: _* )
