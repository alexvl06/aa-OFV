// ---------------------
// Settings
// ---------------------

Common.commonSettings

name := "persistence"

libraryDependencies ++= {
  import Dependencies._
  dbLibs ++ slickLibs
}

// -----------------------
// Custom settings
// -----------------------
