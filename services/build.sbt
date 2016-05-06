import Common._

name := "service"

Common.commonSettings

libraryDependencies ++= {
  import Dependencies._
  recaptchaLibs
}


