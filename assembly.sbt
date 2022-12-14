import sbt.Keys._

// ---------------------
// Settings
// ---------------------

aggregate in assembly := false

assemblyMergeStrategy in assembly := {
  //case "reference.conf" => MergeStrategy.concat
  case PathList("com", "github", "tminglei", "slickpg", xs@_*) => MergeStrategy.first
  case PathList("com", "sun", "mail", "smtp", xs@_*) => MergeStrategy.first
  case PathList("org", "postgresql", xs@_*) => MergeStrategy.first
  case PathList("org", "apache", "commons", xs@_*) => MergeStrategy.first

  case PathList("META-INF", xs@_*) => xs.map(_.toLowerCase) match {
    case ("mailcap" :: Nil) => MergeStrategy.first
    case _ => MergeStrategy.discard
  }

  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}

assemblyOutputPath in assembly := file(s"../${name.value}_${scalaBinaryVersion.value}-${(version in ThisBuild).value}.jar")

mainClass in assembly := Option("co.com.alianza.app.Boot")

test in assembly := {}

// -----------------------
// Custom settings
// -----------------------
