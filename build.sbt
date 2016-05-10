import Common._
import sbt.Keys._
import sbt._

val appSettings: Seq[Setting[_]] = commonSettings ++  Seq(
  name := "fiduciaria-autenticacion-autorizacion",

  mainClass in reStart := Option("co.com.alianza.app.Boot"),

  mainClass in (Compile, run) := Option("co.com.alianza.app.Boot")
)

val dependsOnTest = "test->test;compile->compile"

val common = project in file("common")

val persistence = (project in file("persistence")).aggregate(common).dependsOn(common % dependsOnTest)

val services = (project in file("services")).aggregate(persistence).dependsOn(persistence % dependsOnTest)

val root = (project in file(".")).aggregate(services).dependsOn(services).settings(appSettings: _*)

// TEST