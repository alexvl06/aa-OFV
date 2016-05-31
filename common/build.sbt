// ---------------------
// Settings
// ---------------------

Common.commonSettings

name := "common"

libraryDependencies ++= {
  import Dependencies._
  scalaLibs ++ akkaLibs ++ sprayLibs ++ kafkaLibs ++ functionalLibs ++ utilLibs ++ loggingLibs ++ testLibs
}

// -----------------------
// Custom settings
// -----------------------
