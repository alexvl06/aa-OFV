import Common._
import sbt.Keys._
import sbt._

val appSettings: Seq[Setting[_]] = commonSettings ++  Seq(
  aggregate in assembly := false,

  name := "fiduciaria-autenticacion-autorizacion",

  mainClass in reStart := Option("co.com.alianza.app.Boot"),

  mainClass in (Compile, run) := Option( "co.com.alianza.app.Boot" )
)

val dependsOnTest = "test->test;compile->compile"

val common = project in file("common")

val persistence = (project in file("persistence"))
  .dependsOn(common % dependsOnTest)

val services = (project in file("services"))
  .dependsOn(common % dependsOnTest, persistence % dependsOnTest)

val root = (project in file("."))
  .aggregate(common, persistence, services)
  .dependsOn(common, persistence, services)
  .settings(appSettings: _*)
