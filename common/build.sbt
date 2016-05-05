name := "common"

Common.commonSettings

libraryDependencies ++= {
  import Dependencies._
  scalaLibs ++ akkaLibs ++ sprayLibs ++ kafkaLibs ++ functionalLibs ++ utilLibs ++ moduleCommonLibs ++ testLibs
}
