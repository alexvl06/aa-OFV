import sbt._
import Keys._
import Common._

val appSettings: Seq[Setting[_]] = commonSettings ++  Seq(
  name := "autenticacion-autorizacion"
)

val dependsOnTest = "test->test;compile->compile"

val `auth-common` = project in file("common")

val `auth-persistence` = (project in file("persistence"))
  .dependsOn(`auth-common` % dependsOnTest)

val `auth-service` = (project in file("services"))
  .dependsOn(`auth-common` % dependsOnTest, `auth-persistence` % dependsOnTest)

val root = (project in file("."))
  .aggregate(`auth-common`, `auth-persistence`, `auth-service`)
  .dependsOn(`auth-common`, `auth-persistence`, `auth-service`)
  .settings(appSettings: _*)
