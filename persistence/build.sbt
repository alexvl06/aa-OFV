import Common._

name := "persistence"

Common.commonSettings

libraryDependencies ++= {
  import Dependencies._
  dbLibs ++ slickLibs
}
