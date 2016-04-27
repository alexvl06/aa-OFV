import sbt._
import Keys._
import Common._

val appSettings: Seq[Setting[_]] = commonSettings ++  Seq(
  name := "fiduciaria-autenticacion-autorizacion"
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
